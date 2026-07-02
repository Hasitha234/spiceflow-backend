package com.spiceflow.backend.sales.controller;

import org.springframework.validation.annotation.Validated;
import com.spiceflow.backend.auth.entity.User;
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
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody RepRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(salesMasterDataService.createRep(currentUser.getTenantId(), request));
    }

    @GetMapping("/reps")
    @PreAuthorize("hasAuthority('MASTER_DATA_VIEW')")
    @Operation(summary = "List reps")
    public ResponseEntity<Page<RepResponse>> getReps(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return ResponseEntity.ok(salesMasterDataService.getReps(currentUser.getTenantId(), name, pageable));
    }

    // --- DRIVERS ---
    @PostMapping("/drivers")
    @PreAuthorize("hasAuthority('MASTER_DATA_MANAGE')")
    @Operation(summary = "Create a driver")
    public ResponseEntity<DriverResponse> createDriver(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody DriverRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(salesMasterDataService.createDriver(currentUser.getTenantId(), request));
    }

    @GetMapping("/drivers")
    @PreAuthorize("hasAuthority('MASTER_DATA_VIEW')")
    @Operation(summary = "List drivers")
    public ResponseEntity<Page<DriverResponse>> getDrivers(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return ResponseEntity.ok(salesMasterDataService.getDrivers(currentUser.getTenantId(), name, pageable));
    }

    // --- SHOPS ---
    @PostMapping("/shops")
    @PreAuthorize("hasAuthority('MASTER_DATA_MANAGE')")
    @Operation(summary = "Create a shop")
    public ResponseEntity<ShopResponse> createShop(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ShopRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(salesMasterDataService.createShop(currentUser.getTenantId(), request));
    }

    @GetMapping("/shops")
    @PreAuthorize("hasAuthority('MASTER_DATA_VIEW')")
    @Operation(summary = "List shops")
    public ResponseEntity<Page<ShopResponse>> getShops(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String name,
            Pageable pageable) {
        return ResponseEntity.ok(salesMasterDataService.getShops(currentUser.getTenantId(), name, pageable));
    }
}
