package com.spiceflow.backend.sales.dto;

import java.time.OffsetDateTime;
import java.util.List;
import lombok.Builder;

public class QrVerificationDtos {
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
        OffsetDateTime visitedAt, OffsetDateTime qrScannedAt, boolean verified,
        Double latitude, Double longitude,
        List<RepOrderShopInfo> orderDetails
    ) {}

    @Builder
    public record RepOrderShopInfo(String shopName, List<RepOrderItemInfo> items) {}

    @Builder
    public record RepOrderItemInfo(Long productId, String productName, int quantity, java.math.BigDecimal rate, String unitType) {}

    @Builder
    public record LoadingSheetForShopResponse(
        Long loadingSheetId,
        String sheetNumber,
        String driverName,
        String status,
        List<RepOrderItemInfo> items
    ) {}
}
