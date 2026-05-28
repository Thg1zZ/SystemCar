package com.rodalivre.domain.entity;

import com.rodalivre.domain.enums.AdditionalType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "rental_additionals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalAdditional {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rental_id", nullable = false)
    private Rental rental;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AdditionalType type;

    @Column(nullable = false)
    private BigDecimal cost;
}
