package com.rodalivre.api.dto.response;

import com.rodalivre.domain.entity.Vehicle;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class VehicleResponse {
    private UUID id;
    private String brand;
    private String model;
    private Integer year;
    private String licensePlate;
    private String category;
    private String status;
    private BigDecimal dailyRate;
    private Integer mileage;
    private String color;
    private Integer seats;
    private String transmission;
    private String fuelType;
    private String description;
    private UUID locationBranchId;
    private String branchName;

    public static VehicleResponse fromEntity(Vehicle vehicle) {
        VehicleResponse response = new VehicleResponse();
        response.setId(vehicle.getId());
        response.setBrand(vehicle.getBrand());
        response.setModel(vehicle.getModel());
        response.setYear(vehicle.getYear());
        response.setLicensePlate(vehicle.getLicensePlate());
        response.setCategory(vehicle.getCategory() != null ? vehicle.getCategory().name() : null);
        response.setStatus(vehicle.getStatus() != null ? vehicle.getStatus().name() : null);
        response.setDailyRate(vehicle.getDailyRate());
        response.setMileage(vehicle.getMileage());
        response.setColor(vehicle.getColor());
        response.setSeats(vehicle.getSeats());
        response.setTransmission(vehicle.getTransmission() != null ? vehicle.getTransmission().name() : null);
        response.setFuelType(vehicle.getFuelType() != null ? vehicle.getFuelType().name() : null);
        response.setDescription(vehicle.getDescription());
        
        if (vehicle.getLocationBranch() != null) {
            response.setLocationBranchId(vehicle.getLocationBranch().getId());
            response.setBranchName(vehicle.getLocationBranch().getName());
        }
        return response;
    }
}
