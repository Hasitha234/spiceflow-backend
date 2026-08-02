package com.spiceflow.backend.purchase.dto.request;
import lombok.Builder;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Builder
public record PurchaseLineItemRequest(

    
    @NotNull(message = "Product ID is required")
    Long productId,
    
    @NotNull
    @PositiveOrZero
    BigDecimal noOfBoxes,
    
    @NotNull
    @PositiveOrZero
    BigDecimal soldQuantity,
    
    String unitType,
    
    @NotNull
    @PositiveOrZero
    BigDecimal rate,
    
    @PositiveOrZero
    BigDecimal amount



) {}