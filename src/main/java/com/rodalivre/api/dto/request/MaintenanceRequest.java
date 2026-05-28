package com.rodalivre.api.dto.request;

import com.rodalivre.domain.enums.MaintenanceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class MaintenanceRequest {
    @NotNull(message = "ID do veículo é obrigatório")
    private UUID vehicleId;

    @NotNull(message = "Data de início é obrigatória")
    private LocalDate startDate;

    private LocalDate estimatedEndDate;

    @NotNull(message = "O tipo de manutenção é obrigatório")
    private MaintenanceType type;

    @NotBlank(message = "A descrição é obrigatória")
    private String description;

    private BigDecimal cost;
    private String workshop;
    private String notes;
}
