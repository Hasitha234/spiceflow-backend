package com.spiceflow.backend.sales.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import lombok.Data;

@Data
@SuppressWarnings("NullAway.Init")
public class ShopReturnRequest {
    
    @NotNull(message = "Product ID is required")
    private Long productId;
    
    @NotNull
    @PositiveOrZero
    private Integer quantity;
    
    private String unitType;
    
    @NotNull
    @PositiveOrZero
    private BigDecimal creditValue;
    
    @NotBlank
    private String returnType;
}

