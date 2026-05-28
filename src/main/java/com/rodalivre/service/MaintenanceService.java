package com.rodalivre.service;

import com.rodalivre.api.dto.request.MaintenanceRequest;
import com.rodalivre.api.dto.response.MaintenanceResponse;
import com.rodalivre.domain.entity.MaintenanceRecord;
import com.rodalivre.domain.entity.Vehicle;
import com.rodalivre.domain.enums.VehicleStatus;
import com.rodalivre.exception.LocadoraException;
import com.rodalivre.exception.VeiculoIndisponivelException;
import com.rodalivre.repository.MaintenanceRecordRepository;
import com.rodalivre.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private final MaintenanceRecordRepository maintenanceRepository;
    private final VehicleRepository vehicleRepository;
    private final AuditLogService auditLogService;

    public List<MaintenanceResponse> getAllMaintenanceRecords() {
        return maintenanceRepository.findAll().stream()
                .map(MaintenanceResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public MaintenanceResponse registerMaintenance(MaintenanceRequest request) {
        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new LocadoraException("Veículo não encontrado"));

        if (vehicle.getStatus() == VehicleStatus.RENTED) {
            throw new VeiculoIndisponivelException("Veículo alugado não pode ir para manutenção");
        }

        vehicle.setStatus(VehicleStatus.MAINTENANCE);
        vehicleRepository.save(vehicle);

        MaintenanceRecord record = MaintenanceRecord.builder()
                .vehicle(vehicle)
                .startDate(request.getStartDate())
                .estimatedEndDate(request.getEstimatedEndDate())
                .type(request.getType())
                .description(request.getDescription())
                .cost(request.getCost())
                .workshop(request.getWorkshop())
                .notes(request.getNotes())
                .build();

        MaintenanceRecord savedRecord = maintenanceRepository.save(record);

        auditLogService.logAction(
                "REGISTER_MAINTENANCE",
                "Vehicle",
                vehicle.getId(),
                "AVAILABLE",
                "MAINTENANCE"
        );

        return MaintenanceResponse.fromEntity(savedRecord);
    }
}
