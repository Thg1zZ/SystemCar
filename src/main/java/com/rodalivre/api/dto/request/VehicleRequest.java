package com.rodalivre.api.dto.request;

import com.rodalivre.domain.enums.FuelType;
import com.rodalivre.domain.enums.Transmission;
import com.rodalivre.domain.enums.VehicleCategory;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class VehicleRequest {
    @NotBlank(message = "A marca é obrigatória")
    private String brand;

    @NotBlank(message = "O modelo é obrigatório")
    private String model;

    @NotNull(message = "O ano é obrigatório")
    private Integer year;

    @NotBlank(message = "A placa é obrigatória")
    private String licensePlate;

    @NotNull(message = "A categoria é obrigatória")
    private VehicleCategory category;

    @NotNull(message = "A taxa diária é obrigatória")
    @Min(value = 0, message = "A taxa diária deve ser positiva")
    private BigDecimal dailyRate;

    private Integer mileage;
    private String color;
    private Integer seats;
    private Transmission transmission;
    private FuelType fuelType;
    private String description;
    
    // UUID of the branch
    @NotNull(message = "A filial de origem é obrigatória")
    private UUID locationBranchId;
}
