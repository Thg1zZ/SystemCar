package com.rodalivre.api.controller;

import com.rodalivre.api.dto.request.RentalRequest;
import com.rodalivre.api.dto.request.ReturnRentalRequest;
import com.rodalivre.api.dto.response.RentalResponse;
import com.rodalivre.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rentals")
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<List<RentalResponse>> getAllRentals() {
        return ResponseEntity.ok(rentalService.getAllRentals());
    }

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('CLIENT', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<List<RentalResponse>> getMyRentals() {
        return ResponseEntity.ok(rentalService.getMyRentals());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENT', 'OPERATOR', 'ADMIN')")
    public ResponseEntity<RentalResponse> createRental(@Valid @RequestBody RentalRequest request) {
        return ResponseEntity.ok(rentalService.createRental(request));
    }

    @PutMapping("/{id}/return")
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<RentalResponse> returnRental(
            @PathVariable java.util.UUID id,
            @Valid @RequestBody ReturnRentalRequest request
    ) {
        return ResponseEntity.ok(rentalService.returnRental(id, request));
    }
}
