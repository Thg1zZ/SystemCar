package com.rodalivre.service;

import com.rodalivre.api.dto.request.UpdatePasswordRequest;
import com.rodalivre.api.dto.request.UpdateAvatarRequest;
import com.rodalivre.api.dto.response.UserProfileResponse;
import com.rodalivre.domain.entity.User;
import com.rodalivre.exception.LocadoraException;
import com.rodalivre.repository.UserRepository;
import com.rodalivre.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileResponse getUserProfile() {
        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new LocadoraException("Usuário não encontrado"));
        return UserProfileResponse.fromEntity(user);
    }

    @Transactional
    public void updatePassword(UpdatePasswordRequest request) {
        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new LocadoraException("Usuário não encontrado"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new LocadoraException("Senha atual incorreta!");
        }

        if (request.getNewPassword().length() < 6) {
            throw new LocadoraException("A nova senha deve ter pelo menos 6 caracteres!");
        }

        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Transactional
    public void updateAvatar(UpdateAvatarRequest request) {
        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new LocadoraException("Usuário não encontrado"));

        user.setAvatar(request.getAvatar());
        userRepository.save(user);
    }
}
