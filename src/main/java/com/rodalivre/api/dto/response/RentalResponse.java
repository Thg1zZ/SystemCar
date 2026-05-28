package com.rodalivre.api.dto.response;

import com.rodalivre.domain.entity.Rental;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class RentalResponse {
    private UUID id;
    private UUID userId;
    private UUID vehicleId;
    private String vehicleModel;
    private String licensePlate;
    private LocalDateTime pickupDate;
    private LocalDateTime returnDate;
    private String status;
    private BigDecimal baseCost;
    private BigDecimal additionalsCost;
    private BigDecimal lateFee;
    private BigDecimal discount;
    private BigDecimal totalCost;

    public static RentalResponse fromEntity(Rental rental) {
        RentalResponse response = new RentalResponse();
        response.setId(rental.getId());
        response.setUserId(rental.getUser().getId());
        response.setVehicleId(rental.getVehicle().getId());
        response.setVehicleModel(rental.getVehicle().getModel());
        response.setLicensePlate(rental.getVehicle().getLicensePlate());
        response.setPickupDate(rental.getPickupDate());
        response.setReturnDate(rental.getReturnDate());
        response.setStatus(rental.getStatus().name());
        response.setBaseCost(rental.getBaseCost());
        response.setAdditionalsCost(rental.getAdditionalsCost());
        response.setLateFee(rental.getLateFee());
        response.setDiscount(rental.getDiscount());
        response.setTotalCost(rental.getTotalCost());
        return response;
    }
}
