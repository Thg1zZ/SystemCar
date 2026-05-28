package com.rodalivre.domain.entity;

import com.rodalivre.domain.enums.FuelType;
import com.rodalivre.domain.enums.Transmission;
import com.rodalivre.domain.enums.VehicleCategory;
import com.rodalivre.domain.enums.VehicleStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vehicles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String brand;

    @Column(nullable = false, length = 100)
    private String model;

    @Column(name = "\"year\"", nullable = false)
    private Integer year;

    @Column(name = "license_plate", nullable = false, unique = true, length = 10)
    private String licensePlate;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private VehicleCategory category;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private VehicleStatus status = VehicleStatus.AVAILABLE;

    @Column(name = "daily_rate", nullable = false)
    private BigDecimal dailyRate;

    @Builder.Default
    private Integer mileage = 0;

    @Column(length = 50)
    private String color;

    private Integer seats;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Transmission transmission;

    @Enumerated(EnumType.STRING)
    @Column(name = "fuel_type", length = 50)
    private FuelType fuelType;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "jsonb")
    private String features;

    @Column(name = "image_urls", columnDefinition = "jsonb")
    private String imageUrls;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch locationBranch;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
