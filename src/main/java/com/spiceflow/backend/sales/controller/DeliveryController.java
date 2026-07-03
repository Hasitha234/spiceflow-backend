package com.spiceflow.backend.sales.controller;

import org.springframework.validation.annotation.Validated;
import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.sales.dto.request.CreateDeliveryRequest;
import com.spiceflow.backend.sales.dto.request.RecordShopDeliveryRequest;
import com.spiceflow.backend.sales.dto.response.DeliveryResponse;
import com.spiceflow.backend.sales.dto.response.DeliveryShopResponse;
import com.spiceflow.backend.sales.service.DeliveryService;
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
@RequestMapping("/api/v1/sales/deliveries")
@RequiredArgsConstructor
@Tag(name = "Deliveries", description = "Endpoints for managing actual deliveries and payments")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping
    @PreAuthorize("hasAuthority('DELIVERY_WRITE')")
    @Operation(summary = "Start a delivery", description = "Creates a delivery from a confirmed loading sheet")
    public ResponseEntity<DeliveryResponse> createDelivery(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody CreateDeliveryRequest request) {
        log.info("User {} creating delivery", currentUser.getId());
        DeliveryResponse response = deliveryService.createDelivery(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/shops/{shopId}")
    @PreAuthorize("hasAuthority('DELIVERY_WRITE')")
    @Operation(summary = "Record shop delivery", description = "Records delivered items, returns, and payments for a specific shop")
    public ResponseEntity<DeliveryShopResponse> recordShopDelivery(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id,
            @PathVariable Long shopId,
            @Valid @RequestBody RecordShopDeliveryRequest request) {
        log.info("User {} recording delivery {} for shop {}", currentUser.getId(), id, shopId);
        DeliveryShopResponse response = deliveryService.recordShopDelivery(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), id, shopId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAuthority('DELIVERY_WRITE')")
    @Operation(summary = "Complete delivery", description = "Marks the delivery as completed and calculates final totals")
    public ResponseEntity<DeliveryResponse> completeDelivery(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        log.info("User {} completing delivery {}", currentUser.getId(), id);
        DeliveryResponse response = deliveryService.completeDelivery(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DELIVERY_READ')")
    @Operation(summary = "List deliveries", description = "Returns a paginated list of deliveries")
    public ResponseEntity<Page<DeliveryResponse>> getDeliveries(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            Pageable pageable) {
        log.info("User {} listing deliveries", currentUser.getId());
        Page<DeliveryResponse> response = deliveryService.getDeliveries(java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"), pageable);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('DELIVERY_READ')")
    @Operation(summary = "Get delivery by ID", description = "Returns details of a specific delivery")
    public ResponseEntity<DeliveryResponse> getDelivery(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long id) {
        log.info("User {} getting delivery {}", currentUser.getId(), id);
        DeliveryResponse response = deliveryService.getDelivery(id, java.util.Objects.requireNonNull(currentUser.getTenantId(), "Tenant ID cannot be null"));
        return ResponseEntity.ok(response);
    }
}


