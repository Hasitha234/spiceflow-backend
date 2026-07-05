package com.spiceflow.backend.sales.controller;

import org.springframework.validation.annotation.Validated;
import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.sales.dto.request.DriverRequest;
import com.spiceflow.backend.sales.dto.request.RepRequest;
import com.spiceflow.backend.sales.dto.request.ShopRequest;
import com.spiceflow.backend.sales.dto.response.DriverResponse;
import com.spiceflow.backend.sales.dto.response.RepResponse;
import com.spiceflow.backend.sales.dto.response.ShopResponse;
import com.spiceflow.backend.sales.service.SalesMasterDataService;
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
@RequestMapping("/api/v1/sales/master-data")
@RequiredArgsConstructor
@Tag(name = "Sales Master Data", description = "Endpoints for managing shops, reps, and drivers")
public class SalesMasterDataController {

    private final SalesMasterDataService salesMasterDataService;

    // --- REPS ---
    @PostMapping("/reps")
    @PreAuthorize("hasAuthority('SETTINGS_REPS')")
    @Operation(summary = "Create a rep", operationId = "createRep")
    public ResponseEntity<RepResponse> createRep(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody RepRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(salesMasterDataService.createRep(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request));
    }

    @GetMapping("/reps")
    @PreAuthorize("hasAuthority('SETTINGS_REPS')")
    @Operation(summary = "List reps", operationId = "getReps")
    public ResponseEntity<Page<RepResponse>> getReps(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return ResponseEntity.ok(salesMasterDataService.getReps(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), name, pageable));
    }

    @GetMapping("/reps/{id}")
    @PreAuthorize("hasAuthority('SETTINGS_REPS')")
    @Operation(summary = "Get a rep by ID", operationId = "getRep")
    public ResponseEntity<RepResponse> getRep(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(salesMasterDataService.getRep(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null")));
    }

    @PutMapping("/reps/{id}")
    @PreAuthorize("hasAuthority('SETTINGS_REPS')")
    @Operation(summary = "Update a rep", operationId = "updateRep")
    public ResponseEntity<RepResponse> updateRep(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @Valid @RequestBody RepRequest request) {
        return ResponseEntity.ok(salesMasterDataService.updateRep(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request));
    }

    @DeleteMapping("/reps/{id}")
    @PreAuthorize("hasAuthority('SETTINGS_REPS')")
    @Operation(summary = "Delete a rep", operationId = "deleteRep")
    public ResponseEntity<Void> deleteRep(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        salesMasterDataService.deleteRep(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"));
        return ResponseEntity.noContent().build();
    }

    // --- DRIVERS ---
    @PostMapping("/drivers")
    @PreAuthorize("hasAuthority('SETTINGS_DRIVERS')")
    @Operation(summary = "Create a driver", operationId = "createDriver")
    public ResponseEntity<DriverResponse> createDriver(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody DriverRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(salesMasterDataService.createDriver(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request));
    }

    @GetMapping("/drivers")
    @PreAuthorize("hasAuthority('SETTINGS_DRIVERS')")
    @Operation(summary = "List drivers", operationId = "getDrivers")
    public ResponseEntity<Page<DriverResponse>> getDrivers(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return ResponseEntity.ok(salesMasterDataService.getDrivers(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), name, pageable));
    }

    @GetMapping("/drivers/{id}")
    @PreAuthorize("hasAuthority('SETTINGS_DRIVERS')")
    @Operation(summary = "Get a driver by ID", operationId = "getDriver")
    public ResponseEntity<DriverResponse> getDriver(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(salesMasterDataService.getDriver(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null")));
    }

    @PutMapping("/drivers/{id}")
    @PreAuthorize("hasAuthority('SETTINGS_DRIVERS')")
    @Operation(summary = "Update a driver", operationId = "updateDriver")
    public ResponseEntity<DriverResponse> updateDriver(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @Valid @RequestBody DriverRequest request) {
        return ResponseEntity.ok(salesMasterDataService.updateDriver(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request));
    }

    @DeleteMapping("/drivers/{id}")
    @PreAuthorize("hasAuthority('SETTINGS_DRIVERS')")
    @Operation(summary = "Delete a driver", operationId = "deleteDriver")
    public ResponseEntity<Void> deleteDriver(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        salesMasterDataService.deleteDriver(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"));
        return ResponseEntity.noContent().build();
    }

    // --- SHOPS ---
    @PostMapping("/shops")
    @PreAuthorize("hasAuthority('SETTINGS_SHOPS')")
    @Operation(summary = "Create a shop", operationId = "createShop")
    public ResponseEntity<ShopResponse> createShop(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody ShopRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(salesMasterDataService.createShop(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request));
    }

    @GetMapping("/shops")
    @PreAuthorize("hasAuthority('SETTINGS_SHOPS')")
    @Operation(summary = "List shops", operationId = "getShops")
    public ResponseEntity<Page<ShopResponse>> getShops(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return ResponseEntity.ok(salesMasterDataService.getShops(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), name, pageable));
    }

    @GetMapping("/shops/{id}")
    @PreAuthorize("hasAuthority('SETTINGS_SHOPS')")
    @Operation(summary = "Get a shop by ID", operationId = "getShop")
    public ResponseEntity<ShopResponse> getShop(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        return ResponseEntity.ok(salesMasterDataService.getShop(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null")));
    }

    @PutMapping("/shops/{id}")
    @PreAuthorize("hasAuthority('SETTINGS_SHOPS')")
    @Operation(summary = "Update a shop", operationId = "updateShop")
    public ResponseEntity<ShopResponse> updateShop(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @Valid @RequestBody ShopRequest request) {
        return ResponseEntity.ok(salesMasterDataService.updateShop(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request));
    }

    @DeleteMapping("/shops/{id}")
    @PreAuthorize("hasAuthority('SETTINGS_SHOPS')")
    @Operation(summary = "Delete a shop", operationId = "deleteShop")
    public ResponseEntity<Void> deleteShop(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        salesMasterDataService.deleteShop(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"));
        return ResponseEntity.noContent().build();
    }
}


