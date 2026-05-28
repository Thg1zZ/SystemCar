package com.rodalivre.api.dto.response;

import com.rodalivre.domain.entity.MaintenanceRecord;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class MaintenanceResponse {
    private UUID id;
    private UUID vehicleId;
    private String licensePlate;
    private LocalDate startDate;
    private LocalDate estimatedEndDate;
    private LocalDate actualEndDate;
    private String type;
    private String description;
    private BigDecimal cost;
    private String workshop;
    private String notes;

    public static MaintenanceResponse fromEntity(MaintenanceRecord record) {
        MaintenanceResponse response = new MaintenanceResponse();
        response.setId(record.getId());
        response.setVehicleId(record.getVehicle().getId());
        response.setLicensePlate(record.getVehicle().getLicensePlate());
        response.setStartDate(record.getStartDate());
        response.setEstimatedEndDate(record.getEstimatedEndDate());
        response.setActualEndDate(record.getActualEndDate());
        response.setType(record.getType().name());
        response.setDescription(record.getDescription());
        response.setCost(record.getCost());
        response.setWorkshop(record.getWorkshop());
        response.setNotes(record.getNotes());
        return response;
    }
}
