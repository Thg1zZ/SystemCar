package com.rodalivre.api.dto.response;

import com.rodalivre.domain.entity.User;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class LgpdDataResponse {
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
    private Boolean termsAccepted;
    private LocalDateTime termsAcceptedAt;
    private Boolean inadimplente;
    private String role;
    private Boolean active;

    public static LgpdDataResponse fromEntity(User user) {
        return LgpdDataResponse.builder()
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
                .termsAccepted(user.getTermsAccepted())
                .termsAcceptedAt(user.getTermsAcceptedAt())
                .inadimplente(user.getInadimplente())
                .role(user.getRole().name())
                .active(user.getActive())
                .build();
    }
}
