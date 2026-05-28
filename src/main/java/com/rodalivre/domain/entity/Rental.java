package com.rodalivre.domain.entity;

import com.rodalivre.domain.enums.RentalStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "rentals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id", nullable = false)
    private Vehicle vehicle;

    @Column(name = "pickup_date", nullable = false)
    private LocalDateTime pickupDate;

    @Column(name = "return_date", nullable = false)
    private LocalDateTime returnDate;

    @Column(name = "actual_return_date")
    private LocalDateTime actualReturnDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pickup_branch_id")
    private Branch pickupLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_branch_id")
    private Branch returnLocation;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private RentalStatus status = RentalStatus.PENDING;

    @Column(name = "base_cost")
    private BigDecimal baseCost;

    @Builder.Default
    @Column(name = "additionals_cost")
    private BigDecimal additionalsCost = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "late_fee")
    private BigDecimal lateFee = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "total_cost")
    private BigDecimal totalCost;

    @Column(name = "initial_mileage")
    private Integer initialMileage;

    @Column(name = "final_mileage")
    private Integer finalMileage;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Builder.Default
    @OneToMany(mappedBy = "rental", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RentalAdditional> additionals = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
