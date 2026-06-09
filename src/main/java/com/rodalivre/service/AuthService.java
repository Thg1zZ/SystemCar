package com.rodalivre.service;

import com.rodalivre.api.dto.request.LoginRequest;
import com.rodalivre.api.dto.request.RegisterRequest;
import com.rodalivre.api.dto.response.JwtResponse;
import com.rodalivre.domain.entity.User;
import com.rodalivre.domain.enums.UserRole;
import com.rodalivre.repository.UserRepository;
import com.rodalivre.security.JwtTokenProvider;
import com.rodalivre.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import com.rodalivre.exception.LocadoraException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final com.rodalivre.repository.RefreshTokenRepository refreshTokenRepository;

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

    @org.springframework.transaction.annotation.Transactional
    public JwtResponse login(LoginRequest loginRequest, jakarta.servlet.http.HttpServletResponse response) {
        JwtResponse jwtResponse = authenticateUser(loginRequest);
        User user = userRepository.findById(jwtResponse.getId())
                .orElseThrow(() -> new LocadoraException("Usuário não encontrado"));
        
        String refreshToken = createAndPersistRefreshToken(user);
        setRefreshTokenCookie(response, refreshToken);
        
        return jwtResponse;
    }

    @org.springframework.transaction.annotation.Transactional
    public String createAndPersistRefreshToken(User user) {
        byte[] randomBytes = new byte[64];
        new java.security.SecureRandom().nextBytes(randomBytes);
        String refreshToken = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        String tokenHash = hashSha256(refreshToken);
        java.time.LocalDateTime expiryDate = java.time.LocalDateTime.now().plusDays(7);

        // Deleta tokens antigos do mesmo usuario para nao poluir o banco
        refreshTokenRepository.deleteByUser(user);

        com.rodalivre.domain.entity.RefreshToken tokenEntity = com.rodalivre.domain.entity.RefreshToken.builder()
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
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(java.nio.charset.StandardCharsets.UTF_8));
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

    @org.springframework.transaction.annotation.Transactional
    public String refreshAccessToken(String refreshToken, jakarta.servlet.http.HttpServletResponse response) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new LocadoraException("Refresh token ausente.");
        }

        String tokenHash = hashSha256(refreshToken);
        com.rodalivre.domain.entity.RefreshToken tokenEntity = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new LocadoraException("Refresh token inválido ou não encontrado."));

        if (tokenEntity.isRevoked() || tokenEntity.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
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
            new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities()
            );

        return jwtTokenProvider.generateToken(authentication);
    }

    @org.springframework.transaction.annotation.Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            String tokenHash = hashSha256(refreshToken);
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(refreshTokenRepository::delete);
        }
    }

    public void setRefreshTokenCookie(jakarta.servlet.http.HttpServletResponse response, String refreshToken) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // O Render usa HTTPS por padrao
        cookie.setPath("/api/v1/auth"); // Restringe o cookie ao endpoint de autenticacao
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 dias em segundos
        cookie.setAttribute("SameSite", "Lax"); // Mitigacao de CSRF
        response.addCookie(cookie);
    }

    public void clearRefreshTokenCookie(jakarta.servlet.http.HttpServletResponse response) {
        jakarta.servlet.http.Cookie cookie = new jakarta.servlet.http.Cookie("refreshToken", null);
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

        if (!com.rodalivre.util.Validador.isCpfValido(registerRequest.getCpf())) {
            throw new LocadoraException("Erro: CPF inválido. Certifique-se de fornecer um CPF com dígitos verificadores corretos.");
        }

        if (!com.rodalivre.util.Validador.isCnhValida(registerRequest.getCnh())) {
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
                .termsAcceptedAt(java.time.LocalDateTime.now())
                .role(UserRole.CLIENT)
                .build();

        userRepository.save(user);
    }
}
