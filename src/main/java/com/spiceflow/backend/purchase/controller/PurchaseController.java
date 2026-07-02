package com.spiceflow.backend.purchase.controller;

import org.springframework.validation.annotation.Validated;
import com.spiceflow.backend.auth.entity.User;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
    @Operation(summary = "Create a purchase", description = "Creates a new purchase record in DRAFT status")
    public ResponseEntity<PurchaseResponse> createPurchase(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody CreatePurchaseRequest request) {
        log.info("User {} creating purchase", currentUser.getId());
        PurchaseResponse response = purchaseService.createPurchase(currentUser.getTenantId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PURCHASE_VIEW')")
    @Operation(summary = "List all purchases", description = "Returns a paginated list of purchases")
    public ResponseEntity<Page<PurchaseResponse>> getPurchases(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) String invoiceNo,
            Pageable pageable) {
        log.info("User {} listing purchases", currentUser.getId());
        Page<PurchaseResponse> response = purchaseService.getPurchases(currentUser.getTenantId(), invoiceNo, pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASE_VIEW')")
    @Operation(summary = "Get purchase by ID", description = "Returns details of a specific purchase")
    public ResponseEntity<PurchaseResponse> getPurchase(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        log.info("User {} getting purchase {}", currentUser.getId(), id);
        PurchaseResponse response = purchaseService.getPurchase(id, currentUser.getTenantId());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('PURCHASE_UPDATE')")
    @Operation(summary = "Confirm purchase", description = "Confirms a draft purchase and updates inventory in the MAIN store")
    public ResponseEntity<PurchaseResponse> confirmPurchase(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long id) {
        log.info("User {} confirming purchase {}", currentUser.getId(), id);
        PurchaseResponse response = purchaseService.confirmPurchase(id, currentUser.getTenantId());
        return ResponseEntity.ok(response);
    }
}
