package com.rodalivre.service;

import com.rodalivre.api.dto.request.LoginRequest;
import com.rodalivre.api.dto.request.RegisterRequest;
import com.rodalivre.api.dto.response.JwtResponse;
import com.rodalivre.domain.entity.User;
import com.rodalivre.domain.entity.RefreshToken;
import com.rodalivre.domain.enums.UserRole;
import com.rodalivre.repository.UserRepository;
import com.rodalivre.repository.RefreshTokenRepository;
import com.rodalivre.security.JwtTokenProvider;
import com.rodalivre.security.UserDetailsImpl;
import com.rodalivre.util.Validador;
import com.rodalivre.exception.LocadoraException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    public JwtResponse authenticateUser(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtTokenProvider.generateToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return new JwtResponse(jwt, userDetails.getId(), userDetails.getUsername(), userDetails.getFullName(), roles);
    }

    @Transactional
    public JwtResponse login(LoginRequest loginRequest, HttpServletResponse response) {
        JwtResponse jwtResponse = authenticateUser(loginRequest);
        User user = userRepository.findById(jwtResponse.getId())
                .orElseThrow(() -> new LocadoraException("Usuário não encontrado"));
        
        String refreshToken = createAndPersistRefreshToken(user);
        setRefreshTokenCookie(response, refreshToken);
        
        return jwtResponse;
    }

    @Transactional
    public String createAndPersistRefreshToken(User user) {
        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);
        String refreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        String tokenHash = hashSha256(refreshToken);
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(7);

        // Deleta tokens antigos do mesmo usuario para nao poluir o banco
        refreshTokenRepository.deleteByUser(user);

        RefreshToken tokenEntity = RefreshToken.builder()
                .user(user)
                .tokenHash(tokenHash)
                .expiryDate(expiryDate)
                .revoked(false)
                .build();

        refreshTokenRepository.save(tokenEntity);

        return refreshToken;
    }

    private String hashSha256(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao calcular hash do token", e);
        }
    }

    @Transactional
    public String refreshAccessToken(String refreshToken, HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new LocadoraException("Refresh token ausente.");
        }

        String tokenHash = hashSha256(refreshToken);
        RefreshToken tokenEntity = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new LocadoraException("Refresh token inválido ou não encontrado."));

        if (tokenEntity.isRevoked() || tokenEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
            // Se o token foi revogado ou expirou, limpa todos os tokens do usuario por seguranca (reuso suspeito)
            refreshTokenRepository.deleteByUser(tokenEntity.getUser());
            throw new LocadoraException("Refresh token expirado ou revogado.");
        }

        // Rotacao de Refresh Token (uso unico)
        // Deleta o token antigo
        refreshTokenRepository.delete(tokenEntity);

        // Gera um novo refresh token
        String newRefreshToken = createAndPersistRefreshToken(tokenEntity.getUser());
        // Seta o novo cookie
        setRefreshTokenCookie(response, newRefreshToken);

        // Gera novo access token
        UserDetailsImpl userDetails = UserDetailsImpl.build(tokenEntity.getUser());
        Authentication authentication = 
            new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );

        return jwtTokenProvider.generateToken(authentication);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            String tokenHash = hashSha256(refreshToken);
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(refreshTokenRepository::delete);
        }
    }

    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // O Render usa HTTPS por padrao
        cookie.setPath("/api/v1/auth"); // Restringe o cookie ao endpoint de autenticacao
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 dias em segundos
        cookie.setAttribute("SameSite", "Lax"); // Mitigacao de CSRF
        response.addCookie(cookie);
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("refreshToken", null);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/v1/auth");
        cookie.setMaxAge(0); // Expirar imediatamente
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    public void registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new LocadoraException("Erro: Email já está em uso!");
        }

        if (userRepository.existsByCpf(registerRequest.getCpf())) {
            throw new LocadoraException("Erro: CPF já está cadastrado!");
        }

        if (!Validador.isCpfValido(registerRequest.getCpf())) {
            throw new LocadoraException("Erro: CPF inválido. Certifique-se de fornecer um CPF com dígitos verificadores corretos.");
        }

        if (!Validador.isCnhValida(registerRequest.getCnh())) {
            throw new LocadoraException("Erro: CNH inválida. Certifique-se de fornecer uma CNH válida de 11 dígitos.");
        }

        String password = registerRequest.getPassword();
        if (password == null || password.length() < 12 ||
            !password.matches(".*[A-Z].*") ||
            !password.matches(".*\\d.*") ||
            !password.matches(".*[^A-Za-z0-9].*")) {
            throw new LocadoraException("Erro: A senha deve ter pelo menos 12 caracteres, incluindo uma letra maiúscula, um número e um caractere especial.");
        }

        if (java.time.temporal.ChronoUnit.YEARS.between(registerRequest.getBirthDate(), java.time.LocalDate.now()) < 18) {
            throw new LocadoraException("Erro: O usuário deve ter pelo menos 18 anos para se cadastrar.");
        }

        User user = User.builder()
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .passwordHash(passwordEncoder.encode(registerRequest.getPassword()))
                .cpf(registerRequest.getCpf())
                .cnh(registerRequest.getCnh())
                .cnhExpirationDate(registerRequest.getCnhExpirationDate())
                .inadimplente(false)
                .phone(registerRequest.getPhone())
                .birthDate(registerRequest.getBirthDate())
                .termsAccepted(registerRequest.getTermsAccepted())
                .termsAcceptedAt(LocalDateTime.now())
                .role(UserRole.CLIENT)
                .build();

        userRepository.save(user);
    }
}
