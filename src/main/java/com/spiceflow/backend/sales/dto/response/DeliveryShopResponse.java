package com.spiceflow.backend.sales.dto.response;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Builder
public record DeliveryShopResponse(

    
    Long id,
    Long shopId,
    String shopName,
    
    BigDecimal grossBillAmount,
    BigDecimal totalDiscount,
    BigDecimal returnsDeducted,
    BigDecimal netPayable,
    
    BigDecimal paidAmount,
    BigDecimal creditAmount,
    
    OffsetDateTime createdAt,
    OffsetDateTime qrScannedAt,
    
    Boolean locationVerified,
    Double distanceFromShop,
    Double latitude,
    Double longitude,
    String notes,
    
    List<DeliveryShopItemResponse> items,
    List<DeliveryShopReturnResponse> returns,
    List<DeliveryPaymentResponse> payments


) {}