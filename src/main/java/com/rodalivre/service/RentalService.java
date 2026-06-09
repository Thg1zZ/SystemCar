package com.rodalivre.service;

import com.rodalivre.api.dto.request.RentalRequest;
import com.rodalivre.api.dto.request.ReturnRentalRequest;
import com.rodalivre.api.dto.response.RentalResponse;
import com.rodalivre.domain.entity.Branch;
import com.rodalivre.domain.entity.Rental;
import com.rodalivre.domain.entity.User;
import com.rodalivre.domain.entity.Vehicle;
import com.rodalivre.domain.enums.FidelityLevel;
import com.rodalivre.domain.enums.RentalStatus;
import com.rodalivre.domain.enums.VehicleStatus;
import com.rodalivre.exception.LocadoraException;
import com.rodalivre.exception.VeiculoIndisponivelException;
import com.rodalivre.repository.BranchRepository;
import com.rodalivre.repository.RentalRepository;
import com.rodalivre.repository.UserRepository;
import com.rodalivre.repository.VehicleRepository;
import com.rodalivre.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalRepository rentalRepository;
    private final VehicleRepository vehicleRepository;
    private final BranchRepository branchRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    public List<RentalResponse> getAllRentals() {
        return rentalRepository.findAll().stream()
                .map(RentalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<RentalResponse> getMyRentals() {
        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return rentalRepository.findByUserId(currentUser.getId()).stream()
                .map(RentalResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public RentalResponse createRental(RentalRequest request) {
        UserDetailsImpl currentUser = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new LocadoraException("Usuário não encontrado"));

        // Defesa em profundidade: validação de data no Service independente do Bean Validation do DTO.
        // O backend NUNCA confia no frontend — esta verificação garante segurança mesmo em chamadas diretas à API.
        if (request.getPickupDate().isBefore(java.time.LocalDateTime.now())) {
            throw new LocadoraException("Erro: A data de retirada não pode ser no passado.");
        }

        if (request.getReturnDate().isBefore(request.getPickupDate())) {
            throw new LocadoraException("Erro: A data de devolução deve ser posterior à data de retirada.");
        }

        java.time.LocalDate pickupDate = request.getPickupDate().toLocalDate();
        if (user.getCnhExpirationDate() != null && user.getCnhExpirationDate().isBefore(pickupDate)) {
            throw new LocadoraException("Erro: Sua CNH estará vencida na data planejada para a retirada (" + user.getCnhExpirationDate() + ")!");
        }

        if (Boolean.TRUE.equals(user.getInadimplente())) {
            throw new LocadoraException("Erro: Cliente inadimplente! Abertura de aluguel bloqueada.");
        }

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new LocadoraException("Veículo não encontrado"));

        if (vehicle.getStatus() == VehicleStatus.MAINTENANCE) {
            throw new VeiculoIndisponivelException("Veículo em manutenção NÃO pode ser reservado");
        }

        if (rentalRepository.existsOverlappingRentals(vehicle.getId(), request.getPickupDate(), request.getReturnDate())) {
            throw new VeiculoIndisponivelException("Conflito de datas: O veículo já está reservado neste período");
        }

        Branch pickupBranch = branchRepository.findById(request.getPickupBranchId())
                .orElseThrow(() -> new LocadoraException("Filial de retirada não encontrada"));
        Branch returnBranch = branchRepository.findById(request.getReturnBranchId())
                .orElseThrow(() -> new LocadoraException("Filial de devolução não encontrada"));

        long days = ChronoUnit.DAYS.between(request.getPickupDate(), request.getReturnDate());
        if (days == 0) days = 1;

        BigDecimal baseCost = vehicle.getDailyRate().multiply(BigDecimal.valueOf(days));

        Rental rental = Rental.builder()
                .user(user)
                .vehicle(vehicle)
                .pickupDate(request.getPickupDate())
                .returnDate(request.getReturnDate())
                .pickupLocation(pickupBranch)
                .returnLocation(returnBranch)
                .status(RentalStatus.PENDING)
                .initialMileage(vehicle.getMileage() != null ? vehicle.getMileage() : 0)
                .baseCost(baseCost)
                .totalCost(baseCost)
                .build();

        vehicle.setStatus(VehicleStatus.RENTED);
        vehicleRepository.save(vehicle);

        Rental savedRental = rentalRepository.save(rental);

        auditLogService.logAction(
                "CREATE_RENTAL",
                "Rental",
                savedRental.getId(),
                "NONE",
                RentalStatus.PENDING.name()
        );

        return RentalResponse.fromEntity(savedRental);
    }

    @Transactional
    public RentalResponse returnRental(java.util.UUID rentalId, ReturnRentalRequest request) {
        Rental rental = rentalRepository.findById(rentalId)
                .orElseThrow(() -> new LocadoraException("Aluguel não encontrado"));

        if (rental.getStatus() != RentalStatus.ACTIVE && rental.getStatus() != RentalStatus.CONFIRMED) {
            throw new LocadoraException("Apenas aluguéis com status ACTIVE ou CONFIRMED podem ser devolvidos. Status atual: " + rental.getStatus().name());
        }

        // 1. Validar se KM final >= KM inicial
        int initialMileage = rental.getInitialMileage() != null ? rental.getInitialMileage() : 0;
        if (request.getFinalMileage() < initialMileage) {
            throw new LocadoraException("Erro: KM final não pode ser menor que o KM inicial (" + initialMileage + " KM).");
        }

        // Atualizar informações da devolução
        rental.setFinalMileage(request.getFinalMileage());
        rental.setActualReturnDate(java.time.LocalDateTime.now());
        rental.setObservations(request.getObservations());

        // 2. Calcular atraso e respectiva multa
        long daysDelayed = ChronoUnit.DAYS.between(rental.getReturnDate(), rental.getActualReturnDate());
        BigDecimal lateFee = BigDecimal.ZERO;
        if (daysDelayed > 0) {
            BigDecimal dailyRate = rental.getVehicle().getDailyRate();
            // multa = diaria * 1.5 * dias_de_atraso
            lateFee = dailyRate.multiply(BigDecimal.valueOf(1.5)).multiply(BigDecimal.valueOf(daysDelayed));
            rental.setLateFee(lateFee);
        }

        // 3. Calcular desconto de fidelidade baseado no FidelityLevel do usuário
        User user = rental.getUser();
        FidelityLevel level = user.getFidelityLevel();
        double discountPercent = level.getDiscountRate();

        BigDecimal discount = BigDecimal.ZERO;
        if (discountPercent > 0.0) {
            discount = rental.getBaseCost().multiply(BigDecimal.valueOf(discountPercent));
            rental.setDiscount(discount);
        }

        // Custo total = Base + Adicionais + Multa - Desconto
        BigDecimal additionals = rental.getAdditionalsCost() != null ? rental.getAdditionalsCost() : BigDecimal.ZERO;
        BigDecimal totalCost = rental.getBaseCost().add(additionals).add(lateFee).subtract(discount);
        if (totalCost.compareTo(BigDecimal.ZERO) < 0) {
            totalCost = BigDecimal.ZERO;
        }
        rental.setTotalCost(totalCost);

        // 4. Atualizar veículo (KM e status para AVAILABLE)
        Vehicle vehicle = rental.getVehicle();
        vehicle.setMileage(request.getFinalMileage());
        vehicle.setStatus(VehicleStatus.AVAILABLE);
        vehicleRepository.save(vehicle);

        // 5. Conceder pontos de fidelidade (100 pontos por aluguel concluído)
        user.setFidelityPoints(user.getFidelityPoints() + 100);
        userRepository.save(user);

        rental.setStatus(RentalStatus.COMPLETED);
        Rental savedRental = rentalRepository.save(rental);

        auditLogService.logAction(
                "RETURN_RENTAL",
                "Rental",
                savedRental.getId(),
                RentalStatus.ACTIVE.name(),
                RentalStatus.COMPLETED.name()
        );

        return RentalResponse.fromEntity(savedRental);
    }
}
