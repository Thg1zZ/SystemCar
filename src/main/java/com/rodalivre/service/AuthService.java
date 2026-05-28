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

    public void registerUser(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new LocadoraException("Erro: Email já está em uso!");
        }

        if (userRepository.existsByCpf(registerRequest.getCpf())) {
            throw new LocadoraException("Erro: CPF já está cadastrado!");
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
                .role(UserRole.CLIENT)
                .build();

        userRepository.save(user);
    }
}
