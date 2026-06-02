package com.rodalivre.api.controller;

import com.rodalivre.api.dto.request.UpdatePasswordRequest;
import com.rodalivre.api.dto.request.UpdateAvatarRequest;
import com.rodalivre.api.dto.response.UserProfileResponse;
import com.rodalivre.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<UserProfileResponse> getUserProfile() {
        return ResponseEntity.ok(userService.getUserProfile());
    }

    @PutMapping("/password")
    public ResponseEntity<?> updatePassword(@Valid @RequestBody UpdatePasswordRequest request) {
        try {
            userService.updatePassword(request);
            return ResponseEntity.ok("Senha atualizada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/avatar")
    public ResponseEntity<?> updateAvatar(@Valid @RequestBody UpdateAvatarRequest request) {
        try {
            userService.updateAvatar(request);
            return ResponseEntity.ok("Foto de perfil atualizada com sucesso!");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
