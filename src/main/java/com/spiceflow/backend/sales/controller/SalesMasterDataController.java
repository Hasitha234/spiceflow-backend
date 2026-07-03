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
    @PreAuthorize("hasAuthority('MASTER_DATA_MANAGE')")
    @Operation(summary = "Create a rep")
    public ResponseEntity<RepResponse> createRep(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody RepRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(salesMasterDataService.createRep(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request));
    }

    @GetMapping("/reps")
    @PreAuthorize("hasAuthority('MASTER_DATA_VIEW')")
    @Operation(summary = "List reps")
    public ResponseEntity<Page<RepResponse>> getReps(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return ResponseEntity.ok(salesMasterDataService.getReps(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), name, pageable));
    }

    // --- DRIVERS ---
    @PostMapping("/drivers")
    @PreAuthorize("hasAuthority('MASTER_DATA_MANAGE')")
    @Operation(summary = "Create a driver")
    public ResponseEntity<DriverResponse> createDriver(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody DriverRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(salesMasterDataService.createDriver(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request));
    }

    @GetMapping("/drivers")
    @PreAuthorize("hasAuthority('MASTER_DATA_VIEW')")
    @Operation(summary = "List drivers")
    public ResponseEntity<Page<DriverResponse>> getDrivers(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return ResponseEntity.ok(salesMasterDataService.getDrivers(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), name, pageable));
    }

    // --- SHOPS ---
    @PostMapping("/shops")
    @PreAuthorize("hasAuthority('MASTER_DATA_MANAGE')")
    @Operation(summary = "Create a shop")
    public ResponseEntity<ShopResponse> createShop(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody ShopRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(salesMasterDataService.createShop(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request));
    }

    @GetMapping("/shops")
    @PreAuthorize("hasAuthority('MASTER_DATA_VIEW')")
    @Operation(summary = "List shops")
    public ResponseEntity<Page<ShopResponse>> getShops(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return ResponseEntity.ok(salesMasterDataService.getShops(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), name, pageable));
    }
}


