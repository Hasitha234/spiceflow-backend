package com.spiceflow.backend.sales.dto.response;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record RepOrderItemResponse(

    
    Long id,
    Long productId,
    String productName,
    String productSku,
    
    Integer quantity,
    String unitType,
    BigDecimal rate,
    
    BigDecimal grossAmount,
    BigDecimal netAmount,
    
    Boolean isFreeItem,
    Integer boxesNeeded


) {}