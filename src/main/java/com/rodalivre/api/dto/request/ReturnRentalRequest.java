package com.rodalivre.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReturnRentalRequest {
    @NotNull(message = "Quilometragem final é obrigatória")
    @Min(value = 0, message = "Quilometragem final deve ser igual ou maior que zero")
    private Integer finalMileage;

    private String observations;
}
