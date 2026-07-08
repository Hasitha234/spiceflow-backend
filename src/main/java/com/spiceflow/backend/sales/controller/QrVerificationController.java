package com.spiceflow.backend.sales.controller;

import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.sales.dto.QrVerificationDtos.*;
import com.spiceflow.backend.sales.service.QrVerificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Validated
@RequestMapping("/api/v1/sales/qr")
@RequiredArgsConstructor
@Tag(name = "QR Verification", description = "Endpoints for QR code based shop visit verification")
public class QrVerificationController {

    private final QrVerificationService qrVerificationService;

    /**
     * Get QR code data for a shop. The frontend will encode this as a QR code.
     */
    @GetMapping("/shop/{shopId}")
    @PreAuthorize("hasAuthority('DELIVERY_VIEW') or hasAuthority('SHOP_VIEW')")
    @Operation(summary = "Get shop QR data", description = "Returns data to be encoded as a QR code for a shop")
    public ResponseEntity<ShopQrResponse> getShopQrData(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long shopId) {
        Long tenantId = Objects.requireNonNull(currentUser.getTenantId());
        ShopQrResponse response = qrVerificationService.getShopQrData(shopId, tenantId);
        return ResponseEntity.ok(response);
    }

    /**
     * Driver scans a QR code and verifies the shop visit.
     */
    @PostMapping("/verify")
    @PreAuthorize("hasAuthority('DELIVERY_UPDATE') or hasAuthority('DELIVERY_CREATE')")
    @Operation(summary = "Verify shop visit via QR scan", description = "Records a shop visit when driver scans the shop QR code")
    public ResponseEntity<ShopVisitResponse> verifyVisit(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @Valid @RequestBody QrVerifyRequest request) {
        Long tenantId = Objects.requireNonNull(currentUser.getTenantId());
        ShopVisitResponse response = qrVerificationService.verifyVisit(request, tenantId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get all shop visits for a delivery.
     */
    @GetMapping("/delivery/{deliveryId}/visits")
    @PreAuthorize("hasAuthority('DELIVERY_VIEW')")
    @Operation(summary = "Get delivery visits", description = "Returns all QR-verified shop visits for a delivery")
    public ResponseEntity<List<ShopVisitResponse>> getDeliveryVisits(
            @AuthenticationPrincipal AuthenticatedUser currentUser,
            @PathVariable Long deliveryId) {
        Long tenantId = Objects.requireNonNull(currentUser.getTenantId());
        List<ShopVisitResponse> response = qrVerificationService.getDeliveryVisits(deliveryId, tenantId);
        return ResponseEntity.ok(response);
    }
}
