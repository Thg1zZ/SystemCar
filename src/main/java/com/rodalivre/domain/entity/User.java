package com.rodalivre.domain.entity;

import com.rodalivre.domain.enums.FidelityLevel;
import com.rodalivre.domain.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private String cpf;

    @Column(length = 20)
    private String phone;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, length = 20)
    private String cnh;

    @Column(name = "cnh_expiration_date")
    private LocalDate cnhExpirationDate;

    @Builder.Default
    @Column(name = "inadimplente")
    private Boolean inadimplente = false;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserRole role = UserRole.CLIENT;

    @Builder.Default
    @Column(name = "fidelity_points")
    private Integer fidelityPoints = 0;

    @Builder.Default
    private Boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public FidelityLevel getFidelityLevel() {
        return FidelityLevel.fromPoints(this.fidelityPoints != null ? this.fidelityPoints : 0);
    }
}
