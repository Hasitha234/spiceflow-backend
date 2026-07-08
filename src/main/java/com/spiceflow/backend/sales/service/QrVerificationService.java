package com.spiceflow.backend.sales.service;

import com.spiceflow.backend.auth.entity.Tenant;
import com.spiceflow.backend.auth.repository.TenantRepository;
import com.spiceflow.backend.common.exception.ResourceNotFoundException;
import com.spiceflow.backend.sales.dto.QrVerificationDtos.*;
import com.spiceflow.backend.sales.entity.Delivery;
import com.spiceflow.backend.sales.entity.Driver;
import com.spiceflow.backend.sales.entity.Shop;
import com.spiceflow.backend.sales.entity.ShopVisit;
import com.spiceflow.backend.sales.entity.RepOrder;
import com.spiceflow.backend.sales.repository.DeliveryRepository;
import com.spiceflow.backend.sales.repository.ShopRepository;
import com.spiceflow.backend.sales.repository.ShopVisitRepository;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QrVerificationService {

    private final ShopRepository shopRepository;
    private final ShopVisitRepository shopVisitRepository;
    private final DeliveryRepository deliveryRepository;
    private final TenantRepository tenantRepository;

    @Transactional(readOnly = true)
    public ShopQrResponse getShopQrData(Long shopId, Long tenantId) {
        Shop shop = shopRepository.findByIdAndTenantId(shopId, tenantId)
            .orElseThrow(() -> new ResourceNotFoundException("Shop not found"));
        return ShopQrResponse.builder()
            .shopId(shop.getId())
            .shopName(shop.getName())
            .tenantId(tenantId)
            .qrPayload("SPICEFLOW:SHOP:" + tenantId + ":" + shopId)
            .build();
    }

    @Transactional
    public ShopVisitResponse verifyVisit(QrVerifyRequest request, Long tenantId) {
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

        return ShopVisitResponse.builder()
            .visitId(saved.getId())
            .shopId(shop.getId())
            .shopName(shop.getName())
            .visitedAt(saved.getVisitedAt())
            .verified(saved.isVerified())
            .orderDetails(orderDetails)
            .build();
    }

    @Transactional(readOnly = true)
    public List<ShopVisitResponse> getDeliveryVisits(Long deliveryId, Long tenantId) {
        List<ShopVisit> visits = shopVisitRepository.findByDeliveryIdAndTenantId(deliveryId, tenantId);

        return visits.stream()
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
    }
}
