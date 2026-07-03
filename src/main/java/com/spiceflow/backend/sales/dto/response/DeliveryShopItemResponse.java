package com.spiceflow.backend.sales.dto.response;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record DeliveryShopItemResponse(

    
    Long id,
    Long productId,
    String productName,
    String productSku,
    
    Integer quantityDelivered,
    String unitType,
    BigDecimal rate,
    
    BigDecimal grossAmount,
    BigDecimal discountAmount,
    BigDecimal netAmount,
    
    Boolean isFreeItem


) {}