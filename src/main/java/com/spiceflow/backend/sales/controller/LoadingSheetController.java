package com.spiceflow.backend.sales.controller;

import org.springframework.validation.annotation.Validated;
import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.sales.dto.request.CreateLoadingSheetRequest;
import com.spiceflow.backend.sales.dto.response.LoadingSheetResponse;
import com.spiceflow.backend.sales.service.LoadingSheetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Validated
@RequestMapping("/api/v1/sales/loading-sheets")
@RequiredArgsConstructor
@Tag(name = "Loading Sheets", description = "Endpoints for managing loading sheets and lorry loading")
public class LoadingSheetController {

    private final LoadingSheetService loadingSheetService;

    @PostMapping
    @PreAuthorize("hasAuthority('LOADING_CREATE')")
    @Operation(summary = "Create a loading sheet", description = "Generates a loading sheet from a rep order", operationId = "createLoadingSheet")
    public ResponseEntity<LoadingSheetResponse> createLoadingSheet(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateLoadingSheetRequest request) {
        log.info("User {} creating loading sheet", currentUser.getId());
        LoadingSheetResponse response = loadingSheetService.createLoadingSheet(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('LOADING_CONFIRM')")
    @Operation(summary = "Confirm a loading sheet", description = "Confirms loading and transfers inventory to the vehicle", operationId = "confirmLoadingSheet")
    public ResponseEntity<LoadingSheetResponse> confirmLoadingSheet(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        log.info("User {} confirming loading sheet {}", currentUser.getId(), id);
        LoadingSheetResponse response = loadingSheetService.confirmLoadingSheet(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('LOADING_CREATE')")
    @Operation(summary = "Cancel a loading sheet", description = "Cancels a loading sheet and returns rep order to DRAFT / transfers stock to specified warehouse", operationId = "cancelLoadingSheet")
    public ResponseEntity<LoadingSheetResponse> cancelLoadingSheet(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @RequestParam(required = false) Long returnWarehouseId) {
        log.info("User {} cancelling loading sheet {} with returnWarehouseId {}", currentUser.getId(), id, returnWarehouseId);
        LoadingSheetResponse response = loadingSheetService.cancelLoadingSheet(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), returnWarehouseId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('LOADING_VIEW')")
    @Operation(summary = "List loading sheets", description = "Returns a paginated list of loading sheets with optional driver and status filtering", operationId = "getLoadingSheets")
    public ResponseEntity<Page<LoadingSheetResponse>> getLoadingSheets(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) Long driverId,
            @RequestParam(required = false) String status,
            Pageable pageable) {
        log.info("User {} listing loading sheets (driverId={}, status={})", currentUser.getId(), driverId, status);
        Page<LoadingSheetResponse> response = loadingSheetService.getLoadingSheets(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), driverId, status, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('LOADING_VIEW')")
    @Operation(summary = "Get loading sheet by ID", description = "Returns details of a specific loading sheet", operationId = "getLoadingSheet")
    public ResponseEntity<LoadingSheetResponse> getLoadingSheet(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        log.info("User {} getting loading sheet {}", currentUser.getId(), id);
        LoadingSheetResponse response = loadingSheetService.getLoadingSheet(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"));
        return ResponseEntity.ok(response);
    }
}


