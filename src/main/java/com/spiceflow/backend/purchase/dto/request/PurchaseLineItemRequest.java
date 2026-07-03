package com.spiceflow.backend.purchase.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.Data;

@Data
@SuppressWarnings("NullAway.Init")
public class PurchaseLineItemRequest {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull
    @PositiveOrZero
    private Integer noOfBoxes;
    
    @NotNull
    @PositiveOrZero
    private Integer soldQuantity;
    
    private String unitType;
    
    @NotNull
    @PositiveOrZero
    private BigDecimal rate;
}

