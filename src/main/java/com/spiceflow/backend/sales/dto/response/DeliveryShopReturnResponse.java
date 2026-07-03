package com.spiceflow.backend.sales.dto.response;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Builder
public record DeliveryShopReturnResponse(

    
    Long id,
    Long productId,
    String productName,
    String productSku,
    
    Integer quantityReturned,
    String unitType,
    BigDecimal creditValue,
    
    String returnType,
    
    OffsetDateTime createdAt


) {}