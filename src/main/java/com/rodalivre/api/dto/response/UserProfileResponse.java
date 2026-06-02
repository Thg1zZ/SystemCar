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
                .cpf(maskCpf(user.getCpf()))
                .phone(maskPhone(user.getPhone()))
                .birthDate(user.getBirthDate())
                .cnh(maskCnh(user.getCnh()))
                .cnhExpirationDate(user.getCnhExpirationDate())
                .fidelityPoints(user.getFidelityPoints() != null ? user.getFidelityPoints() : 0)
                .fidelityLevel(user.getFidelityLevel().name())
                .avatar(user.getAvatar())
                .build();
    }

    private static String maskCpf(String cpf) {
        if (cpf == null) return null;
        String clean = cpf.replaceAll("\\D", "");
        if (clean.length() < 11) return "***.***.***-**";
        return clean.substring(0, 3) + ".***.***-" + clean.substring(9);
    }

    private static String maskCnh(String cnh) {
        if (cnh == null) return null;
        String clean = cnh.replaceAll("\\D", "");
        if (clean.length() < 11) return "***********";
        return clean.substring(0, 3) + "******" + clean.substring(9);
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) return "";
        String clean = phone.replaceAll("\\D", "");
        if (clean.length() >= 10) {
            return "(" + clean.substring(0, 2) + ") *****-" + clean.substring(clean.length() - 4);
        }
        return phone;
    }
}
