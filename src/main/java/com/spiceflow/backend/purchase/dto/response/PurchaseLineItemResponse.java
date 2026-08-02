package com.spiceflow.backend.purchase.dto.response;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PurchaseLineItemResponse(

    
    Long id,
    Long productId,
    String productName,
    String productSku,
    
    BigDecimal noOfBoxes,
    BigDecimal soldQuantity,
    String unitType,
    BigDecimal rate,
    BigDecimal amount



) {}