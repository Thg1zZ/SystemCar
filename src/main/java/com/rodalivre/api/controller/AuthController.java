package com.rodalivre.api.controller;

import com.rodalivre.api.dto.request.LoginRequest;
import com.rodalivre.api.dto.request.RegisterRequest;
import com.rodalivre.api.dto.response.JwtResponse;
import com.rodalivre.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        JwtResponse jwtResponse = authService.login(loginRequest, response);
        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        try {
            String newAccessToken = authService.refreshAccessToken(refreshToken, response);
            return ResponseEntity.ok(java.util.Map.of("accessToken", newAccessToken));
        } catch (Exception e) {
            authService.clearRefreshTokenCookie(response);
            return ResponseEntity.status(401).body(java.util.Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken,
            jakarta.servlet.http.HttpServletResponse response
    ) {
        authService.logout(refreshToken);
        authService.clearRefreshTokenCookie(response);
        return ResponseEntity.ok(java.util.Map.of("message", "Logout realizado com sucesso!"));
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        authService.registerUser(registerRequest);
        return ResponseEntity.ok(java.util.Map.of("message", "Usuário registrado com sucesso!"));
    }
}
