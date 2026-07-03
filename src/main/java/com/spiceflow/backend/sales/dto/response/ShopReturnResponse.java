package com.spiceflow.backend.sales.dto.response;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
public record ShopReturnResponse(

    
    Long id,
    Long productId,
    String productName,
    String productSku,
    
    Integer quantity,
    String unitType,
    BigDecimal creditValue,
    
    String returnType,
    String status,
    
    OffsetDateTime createdAt


) {}