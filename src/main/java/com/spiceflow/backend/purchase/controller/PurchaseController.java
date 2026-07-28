package com.spiceflow.backend.purchase.controller;

import org.springframework.validation.annotation.Validated;
import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.purchase.dto.request.CreatePurchaseRequest;
import com.spiceflow.backend.purchase.dto.response.PurchaseResponse;
import com.spiceflow.backend.purchase.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Slf4j
@RestController
@Validated
@RequestMapping("/api/v1/purchases")
@RequiredArgsConstructor
@Tag(name = "Purchases", description = "Endpoints for managing supplier purchases")
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping
    @PreAuthorize("hasAuthority('PURCHASE_CREATE')")
    @Operation(summary = "Create a purchase", description = "Creates a new purchase record in DRAFT status", operationId = "createPurchase")
    public ResponseEntity<PurchaseResponse> createPurchase(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreatePurchaseRequest request) {
        log.info("User {} creating purchase", currentUser.getId());
        PurchaseResponse response = purchaseService.createPurchase(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASE_UPDATE')")
    @Operation(summary = "Update a DRAFT purchase", description = "Updates an existing purchase record in DRAFT status", operationId = "updatePurchase")
    public ResponseEntity<PurchaseResponse> updatePurchase(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @Valid @RequestBody CreatePurchaseRequest request) {
        log.info("User {} updating purchase {}", currentUser.getId(), id);
        PurchaseResponse response = purchaseService.updatePurchase(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PURCHASE_VIEW')")
    @Operation(summary = "List all purchases", description = "Returns a paginated list of purchases", operationId = "getPurchases")
    public ResponseEntity<Page<PurchaseResponse>> getPurchases(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @RequestParam(required = false) String invoiceNo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
        log.info("User {} listing purchases", currentUser.getId());
        Page<PurchaseResponse> response = purchaseService.getPurchases(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), invoiceNo, date, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASE_VIEW')")
    @Operation(summary = "Get purchase by ID", description = "Returns details of a specific purchase", operationId = "getPurchase")
    public ResponseEntity<PurchaseResponse> getPurchase(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        log.info("User {} getting purchase {}", currentUser.getId(), id);
        PurchaseResponse response = purchaseService.getPurchase(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('PURCHASE_UPDATE')")
    @Operation(summary = "Confirm purchase", description = "Confirms a draft purchase and updates inventory in the selected warehouse", operationId = "confirmPurchase")
    public ResponseEntity<PurchaseResponse> confirmPurchase(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @RequestParam Long warehouseId) {
        log.info("User {} confirming purchase {} to warehouse {}", currentUser.getId(), id, warehouseId);
        PurchaseResponse response = purchaseService.confirmPurchase(id, warehouseId, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('ROLE_TENANT_OWNER')")
    @Operation(summary = "Cancel confirmed purchase", description = "Reverts a confirmed purchase to DRAFT and undoes inventory updates", operationId = "cancelPurchase")
    public ResponseEntity<PurchaseResponse> cancelPurchase(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        log.info("User {} cancelling/reverting purchase {}", currentUser.getId(), id);
        PurchaseResponse response = purchaseService.cancelPurchase(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"));
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASE_DELETE')")
    @Operation(summary = "Delete purchase", description = "Deletes a draft purchase", operationId = "deletePurchase")
    public ResponseEntity<Void> deletePurchase(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        log.info("User {} deleting purchase {}", currentUser.getId(), id);
        purchaseService.deletePurchase(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"));
        return ResponseEntity.noContent().build();
    }
}


