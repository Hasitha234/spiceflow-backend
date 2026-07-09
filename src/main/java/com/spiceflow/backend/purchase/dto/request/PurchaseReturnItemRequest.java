package com.spiceflow.backend.purchase.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record PurchaseReturnItemRequest(
    
    @NotNull(message = "Product ID is required")
    Long productId,
    
    @NotNull
    @Positive
    Integer quantity,
    
    String unitType,
    
    @NotNull
    @PositiveOrZero
    BigDecimal rate,
    
    @PositiveOrZero
    BigDecimal amount
) {}
