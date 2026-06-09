package com.rodalivre.service;

import com.rodalivre.api.dto.request.VehicleRequest;
import com.rodalivre.api.dto.response.VehicleResponse;
import com.rodalivre.domain.entity.Branch;
import com.rodalivre.domain.entity.Vehicle;
import com.rodalivre.domain.enums.VehicleStatus;
import com.rodalivre.exception.LocadoraException;
import com.rodalivre.repository.BranchRepository;
import com.rodalivre.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final BranchRepository branchRepository;

    public List<VehicleResponse> getAllVehicles(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        return vehicleRepository.findAll(pageable).getContent().stream()
                .map(VehicleResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public VehicleResponse getVehicleById(UUID id) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new LocadoraException("Veículo não encontrado"));
        return VehicleResponse.fromEntity(vehicle);
    }

    @Transactional
    public VehicleResponse createVehicle(VehicleRequest request) {
        if (vehicleRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new LocadoraException("Já existe um veículo cadastrado com a placa " + request.getLicensePlate());
        }

        Branch branch = branchRepository.findById(request.getLocationBranchId())
                .orElseThrow(() -> new LocadoraException("Filial não encontrada"));

        Vehicle vehicle = Vehicle.builder()
                .brand(request.getBrand())
                .model(request.getModel())
                .year(request.getYear())
                .licensePlate(request.getLicensePlate())
                .category(request.getCategory())
                .status(VehicleStatus.AVAILABLE)
                .dailyRate(request.getDailyRate())
                .mileage(request.getMileage() != null ? request.getMileage() : 0)
                .color(request.getColor())
                .seats(request.getSeats())
                .transmission(request.getTransmission())
                .fuelType(request.getFuelType())
                .description(request.getDescription())
                .locationBranch(branch)
                .build();

        Vehicle savedVehicle = vehicleRepository.save(vehicle);
        return VehicleResponse.fromEntity(savedVehicle);
    }

    @Transactional
    public VehicleResponse updateVehicle(UUID id, VehicleRequest request) {
        Vehicle vehicle = vehicleRepository.findById(id)
                .orElseThrow(() -> new LocadoraException("Veículo não encontrado"));

        if (!vehicle.getLicensePlate().equals(request.getLicensePlate()) &&
                vehicleRepository.existsByLicensePlate(request.getLicensePlate())) {
            throw new LocadoraException("Já existe outro veículo cadastrado com a placa " + request.getLicensePlate());
        }

        Branch branch = branchRepository.findById(request.getLocationBranchId())
                .orElseThrow(() -> new LocadoraException("Filial não encontrada"));

        vehicle.setBrand(request.getBrand());
        vehicle.setModel(request.getModel());
        vehicle.setYear(request.getYear());
        vehicle.setLicensePlate(request.getLicensePlate());
        vehicle.setCategory(request.getCategory());
        vehicle.setDailyRate(request.getDailyRate());
        if (request.getMileage() != null) vehicle.setMileage(request.getMileage());
        vehicle.setColor(request.getColor());
        vehicle.setSeats(request.getSeats());
        vehicle.setTransmission(request.getTransmission());
        vehicle.setFuelType(request.getFuelType());
        vehicle.setDescription(request.getDescription());
        vehicle.setLocationBranch(branch);
        // INTENCIONAL: o campo 'status' é omitido neste método propositalmente.
        // O status do veículo é gerenciado EXCLUSIVAMENTE pelo fluxo de negócio:
        //   RentalService.createRental()  → AVAILABLE → RENTED
        //   RentalService.returnRental()  → RENTED    → AVAILABLE
        //   MaintenanceService            → AVAILABLE → MAINTENANCE
        // Incluí-lo aqui quebraria a integridade do ciclo de vida do veículo.

        Vehicle updatedVehicle = vehicleRepository.save(vehicle);
        return VehicleResponse.fromEntity(updatedVehicle);
    }
}
