package com.rodalivre.api.dto.request;

import com.rodalivre.domain.enums.AdditionalType;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class RentalRequest {
    @NotNull(message = "ID do veículo é obrigatório")
    private UUID vehicleId;

    @NotNull(message = "Data de retirada é obrigatória")
    @Future(message = "A data de retirada deve estar no futuro")
    private LocalDateTime pickupDate;

    @NotNull(message = "Data de devolução é obrigatória")
    @Future(message = "A data de devolução deve estar no futuro")
    private LocalDateTime returnDate;

    @NotNull(message = "Filial de retirada é obrigatória")
    private UUID pickupBranchId;

    @NotNull(message = "Filial de devolução é obrigatória")
    private UUID returnBranchId;

    private List<AdditionalType> additionals;
}
