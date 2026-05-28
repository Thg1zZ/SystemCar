package com.rodalivre.service;

import com.rodalivre.api.dto.response.DashboardMetricsResponse;
import com.rodalivre.domain.enums.RentalStatus;
import com.rodalivre.domain.enums.UserRole;
import com.rodalivre.domain.enums.VehicleStatus;
import com.rodalivre.repository.RentalRepository;
import com.rodalivre.repository.UserRepository;
import com.rodalivre.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final VehicleRepository vehicleRepository;
    private final RentalRepository rentalRepository;
    private final UserRepository userRepository;

    public DashboardMetricsResponse getMetrics() {
        long totalVehicles = vehicleRepository.count();
        long availableVehicles = vehicleRepository.countByStatus(VehicleStatus.AVAILABLE);
        long rentedVehicles = vehicleRepository.countByStatus(VehicleStatus.RENTED);
        long inMaintenance = vehicleRepository.countByStatus(VehicleStatus.MAINTENANCE);
        
        long activeRentals = rentalRepository.countByStatus(RentalStatus.ACTIVE);
        long totalCustomers = userRepository.countByRole(UserRole.CLIENT);
        
        BigDecimal currentMonthRevenue = rentalRepository.sumCompletedRentalsRevenue();

        return DashboardMetricsResponse.builder()
                .totalVehicles(totalVehicles)
                .availableVehicles(availableVehicles)
                .rentedVehicles(rentedVehicles)
                .vehiclesInMaintenance(inMaintenance)
                .activeRentals(activeRentals)
                .totalCustomers(totalCustomers)
                .currentMonthRevenue(currentMonthRevenue)
                .build();
    }
}
