package com.spiceflow.backend.sales.dto.request;
import lombok.Builder;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;

@Builder
public record ShopReturnRequest(

    
    @NotNull(message = "Product ID is required")
    Long productId,
    
    @NotNull
    @PositiveOrZero
    Integer quantity,
    
    String unitType,
    
    @NotNull
    @PositiveOrZero
    BigDecimal creditValue,
    
    @NotBlank
    String returnType



) {}