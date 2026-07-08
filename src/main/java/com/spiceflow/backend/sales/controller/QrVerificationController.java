package com.spiceflow.backend.sales.controller;

import com.spiceflow.backend.auth.dto.AuthenticatedUser;
import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.sales.entity.Delivery;
import com.spiceflow.backend.sales.entity.Driver;
import com.spiceflow.backend.sales.entity.Shop;
import com.spiceflow.backend.sales.entity.ShopVisit;
import com.spiceflow.backend.sales.entity.RepOrder;
import com.spiceflow.backend.sales.entity.RepOrderShop;
import com.spiceflow.backend.sales.mapper.RepOrderMapper;
import com.spiceflow.backend.sales.repository.DeliveryRepository;
import com.spiceflow.backend.sales.repository.ShopRepository;
import com.spiceflow.backend.sales.repository.ShopVisitRepository;
import com.spiceflow.backend.sales.service.SalesMasterDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import lombok.Builder;
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

    private final ShopRepository shopRepository;
    private final ShopVisitRepository shopVisitRepository;
    private final DeliveryRepository deliveryRepository;
    private final TenantRepository tenantRepository;

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
        Shop shop = shopRepository.findByIdAndTenantId(shopId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        ShopQrResponse response = ShopQrResponse.builder()
            .shopId(shop.getId())
            .shopName(shop.getName())
            .tenantId(tenantId)
            .qrPayload("SPICEFLOW:SHOP:" + tenantId + ":" + shopId)
            .build();

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
        Tenant tenant = tenantRepository.findById(tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Tenant not found"));

        Shop shop = shopRepository.findByIdAndTenantId(request.shopId(), tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));

        Delivery delivery = null;
        Driver driver = null;
        if (request.deliveryId() != null) {
            delivery = deliveryRepository.findByIdAndTenantId(request.deliveryId(), tenantId)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found"));
            if (delivery.getLoadingSheet() != null) {
                driver = delivery.getLoadingSheet().getDriver();
            }
        }

        ShopVisit visit = ShopVisit.builder()
            .tenant(tenant)
            .delivery(delivery)
            .shop(shop)
            .driver(driver)
            .visitedAt(OffsetDateTime.now(java.time.ZoneId.systemDefault()))
            .qrScannedAt(OffsetDateTime.now(java.time.ZoneId.systemDefault()))
            .latitude(request.latitude())
            .longitude(request.longitude())
            .verified(true)
            .notes(request.notes())
            .build();

        ShopVisit saved = shopVisitRepository.save(visit);
        log.info("Shop visit verified: shop={}, delivery={}, tenant={}", request.shopId(), request.deliveryId(), tenantId);

        // Build response with rep order details if delivery exists
        List<RepOrderShopInfo> orderDetails = List.of();
        if (delivery != null && delivery.getLoadingSheet() != null
            && delivery.getLoadingSheet().getRepOrder() != null) {
            RepOrder repOrder = delivery.getLoadingSheet().getRepOrder();
            orderDetails = repOrder.getShops().stream()
                .filter(s -> s.getShop().getId().equals(request.shopId()))
                .map(s -> RepOrderShopInfo.builder()
                    .shopName(s.getShop().getName())
                    .items(s.getItems().stream().map(i -> RepOrderItemInfo.builder()
                        .productName(i.getProduct().getName())
                        .quantity(i.getQuantity())
                        .rate(i.getRate())
                        .unitType(i.getUnitType())
                        .build()).toList())
                    .build())
                .toList();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(ShopVisitResponse.builder()
            .visitId(saved.getId())
            .shopId(shop.getId())
            .shopName(shop.getName())
            .visitedAt(saved.getVisitedAt())
            .verified(saved.isVerified())
            .orderDetails(orderDetails)
            .build());
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
        List<ShopVisit> visits = shopVisitRepository.findByDeliveryIdAndTenantId(deliveryId, tenantId);

        List<ShopVisitResponse> response = visits.stream()
            .map(v -> ShopVisitResponse.builder()
                .visitId(v.getId())
                .shopId(v.getShop().getId())
                .shopName(v.getShop().getName())
                .visitedAt(v.getVisitedAt())
                .verified(v.isVerified())
                .latitude(v.getLatitude())
                .longitude(v.getLongitude())
                .orderDetails(List.of())
                .build())
            .toList();

        return ResponseEntity.ok(response);
    }

    // ─── Inner DTOs ───────────────────────────────────────────────────────────

    @Builder
    public record ShopQrResponse(Long shopId, String shopName, Long tenantId, String qrPayload) {}

    public record QrVerifyRequest(
        Long shopId,
        Long deliveryId,
        Double latitude,
        Double longitude,
        String notes
    ) {}

    @Builder
    public record ShopVisitResponse(
        Long visitId, Long shopId, String shopName,
        OffsetDateTime visitedAt, boolean verified,
        Double latitude, Double longitude,
        List<RepOrderShopInfo> orderDetails
    ) {}

    @Builder
    public record RepOrderShopInfo(String shopName, List<RepOrderItemInfo> items) {}

    @Builder
    public record RepOrderItemInfo(String productName, int quantity, java.math.BigDecimal rate, String unitType) {}
}
