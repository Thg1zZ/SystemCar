package com.rodalivre.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class DashboardMetricsResponse {
    private long totalVehicles;
    private long availableVehicles;
    private long rentedVehicles;
    private long vehiclesInMaintenance;
    
    private long activeRentals;
    private long totalCustomers;
    
    private BigDecimal currentMonthRevenue;
}
