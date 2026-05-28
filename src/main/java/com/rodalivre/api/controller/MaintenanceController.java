package com.rodalivre.api.controller;

import com.rodalivre.api.dto.request.MaintenanceRequest;
import com.rodalivre.api.dto.response.MaintenanceResponse;
import com.rodalivre.service.MaintenanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/maintenance")
@RequiredArgsConstructor
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    @GetMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<List<MaintenanceResponse>> getAll() {
        return ResponseEntity.ok(maintenanceService.getAllMaintenanceRecords());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPERATOR', 'ADMIN')")
    public ResponseEntity<MaintenanceResponse> register(@Valid @RequestBody MaintenanceRequest request) {
        return ResponseEntity.ok(maintenanceService.registerMaintenance(request));
    }
}
