package com.spiceflow.backend.sales.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class RepOrderItemRequest {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull
    @PositiveOrZero
    private Integer quantity;
    
    private String unitType;
    
    @NotNull
    @PositiveOrZero
    private BigDecimal rate;
    
    @NotNull
    @PositiveOrZero
    private BigDecimal discountAmount;
    
    private Boolean isFreeItem = false;
    
    @NotNull
    @PositiveOrZero
    private Integer boxesNeeded;
}
