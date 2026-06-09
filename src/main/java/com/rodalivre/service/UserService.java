package com.rodalivre.service;

import com.rodalivre.api.dto.request.UpdatePasswordRequest;
import com.rodalivre.api.dto.request.UpdateAvatarRequest;
import com.rodalivre.api.dto.response.UserProfileResponse;
import com.rodalivre.api.dto.response.LgpdDataResponse;
import java.time.LocalDate;
import java.util.UUID;
import com.rodalivre.domain.entity.User;
import com.rodalivre.domain.enums.RentalStatus;
import com.rodalivre.repository.RentalRepository;
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
    private final RentalRepository rentalRepository;
    private final AuditLogService auditLogService;

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

        String newPassword = request.getNewPassword();
        if (newPassword == null || newPassword.length() < 12 ||
            !newPassword.matches(".*[A-Z].*") ||
            !newPassword.matches(".*\\d.*") ||
            !newPassword.matches(".*[^A-Za-z0-9].*")) {
            throw new LocadoraException("A nova senha deve ter pelo menos 12 caracteres, incluindo uma letra maiúscula, um número e um caractere especial.");
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

    public LgpdDataResponse getLgpdData() {
        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new LocadoraException("Usuário não encontrado"));
        return LgpdDataResponse.fromEntity(user);
    }

    @Transactional
    public void excluirContaLgpd() {
        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new LocadoraException("Usuário não encontrado"));

        // Verificar alugueis ativos, pendentes ou confirmados
        boolean possuiAluguelAtivo = rentalRepository.findByUserId(user.getId()).stream()
                .anyMatch(r -> r.getStatus() == RentalStatus.ACTIVE || 
                               r.getStatus() == RentalStatus.CONFIRMED || 
                               r.getStatus() == RentalStatus.PENDING);
        if (possuiAluguelAtivo) {
            throw new LocadoraException("Não é possível excluir a conta pois você possui reservas ativas, confirmadas ou pendentes.");
        }

        // Anonimizacao para o direito ao esquecimento (LGPD) mantendo os registros financeiros de auditoria
        user.setFullName("Usuário Anonimizado");
        user.setEmail("deleted-" + UUID.randomUUID() + "@systemcar.com.br");
        user.setPasswordHash("DELETED_USER_PASSWORD_HASH");
        user.setCpf("00000000000");
        user.setCnh("00000000000");
        user.setPhone(null);
        user.setBirthDate(LocalDate.of(1970, 1, 1));
        user.setAvatar(null);
        user.setActive(false);

        userRepository.save(user);

        auditLogService.logAction(
                "ANONIMIZAR_CONTA",
                "User",
                user.getId(),
                "ACTIVE",
                "ANONIMIZED"
        );
    }
}
