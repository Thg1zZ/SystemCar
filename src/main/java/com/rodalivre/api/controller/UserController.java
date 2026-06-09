package com.rodalivre.api.controller;

import com.rodalivre.api.dto.request.UpdatePasswordRequest;
import com.rodalivre.api.dto.request.UpdateAvatarRequest;
import com.rodalivre.api.dto.response.UserProfileResponse;
import com.rodalivre.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.rodalivre.api.dto.response.LgpdDataResponse;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/api/v1/users/me")
    public ResponseEntity<UserProfileResponse> getUserProfile() {
        return ResponseEntity.ok(userService.getUserProfile());
    }

    @PutMapping("/api/v1/users/me/password")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        try {
            userService.updatePassword(request);
            return ResponseEntity.ok("Senha atualizada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/api/v1/users/me/avatar")
    public ResponseEntity<?> updateAvatar(@Valid @RequestBody UpdateAvatarRequest request) {
        try {
            userService.updateAvatar(request);
            return ResponseEntity.ok("Foto de perfil updated com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping({"/api/v1/users/me/meus-dados", "/api/cliente/meus-dados"})
    public ResponseEntity<LgpdDataResponse> getLgpdData() {
        return ResponseEntity.ok(userService.getLgpdData());
    }

    @DeleteMapping({"/api/v1/users/me/excluir-conta", "/api/cliente/excluir-conta"})
    public ResponseEntity<?> excluirContaLgpd() {
        try {
            userService.excluirContaLgpd();
            return ResponseEntity.ok(java.util.Map.of("message", "Sua conta foi anonimizada com sucesso em conformidade com a LGPD."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", e.getMessage()));
        }
    }
}
