package com.rodalivre.repository;

import com.rodalivre.domain.entity.Rental;
import com.rodalivre.domain.enums.RentalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RentalRepository extends JpaRepository<Rental, UUID> {
    List<Rental> findByUserId(UUID userId);
    
    List<Rental> findByVehicleId(UUID vehicleId);

    @Query("SELECT COUNT(r) > 0 FROM Rental r WHERE r.vehicle.id = :vehicleId " +
           "AND r.status IN ('PENDING', 'CONFIRMED', 'ACTIVE') " +
           "AND ((r.pickupDate BETWEEN :pickup AND :returnDate) " +
           "OR (r.returnDate BETWEEN :pickup AND :returnDate) " +
           "OR (:pickup BETWEEN r.pickupDate AND r.returnDate))")
    boolean existsOverlappingRentals(@Param("vehicleId") UUID vehicleId, 
                                     @Param("pickup") LocalDateTime pickup, 
                                     @Param("returnDate") LocalDateTime returnDate);

    long countByStatus(com.rodalivre.domain.enums.RentalStatus status);

    @Query("SELECT SUM(r.totalCost) FROM Rental r WHERE r.status = com.rodalivre.domain.enums.RentalStatus.COMPLETED")
    java.math.BigDecimal sumCompletedRentalsRevenue();
}
