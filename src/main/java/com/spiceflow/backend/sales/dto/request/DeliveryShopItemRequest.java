package com.spiceflow.backend.sales.dto.request;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Builder
public record DeliveryShopItemRequest(

    
    @NotNull(message = "Product ID is required")
    Long productId,
    
    @NotNull
    @PositiveOrZero
    Integer quantityDelivered,
    
    String unitType,
    
    @NotNull
    @PositiveOrZero
    BigDecimal rate,
    
    @NotNull
    @PositiveOrZero
    BigDecimal discountAmount,
    
    Boolean isFreeItem



) {}