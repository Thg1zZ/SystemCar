package com.rodalivre.api.dto.response;

import com.rodalivre.domain.entity.User;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
public class UserProfileResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String cpf;
    private String phone;
    private LocalDate birthDate;
    private String cnh;
    private LocalDate cnhExpirationDate;
    private Integer fidelityPoints;
    private String fidelityLevel;
    private String avatar;

    public static UserProfileResponse fromEntity(User user) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .cpf(user.getCpf())
                .phone(user.getPhone())
                .birthDate(user.getBirthDate())
                .cnh(user.getCnh())
                .cnhExpirationDate(user.getCnhExpirationDate())
                .fidelityPoints(user.getFidelityPoints() != null ? user.getFidelityPoints() : 0)
                .fidelityLevel(user.getFidelityLevel().name())
                .avatar(user.getAvatar())
                .build();
    }
}
