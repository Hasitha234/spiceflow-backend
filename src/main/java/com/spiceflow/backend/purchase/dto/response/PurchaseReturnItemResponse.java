package com.spiceflow.backend.purchase.dto.response;

import java.math.BigDecimal;
import lombok.Builder;

@Builder
public record PurchaseReturnItemResponse(
    Long id,
    Long productId,
    String productName,
    String productSku,
    Integer quantity,
    String unitType,
    BigDecimal rate,
    BigDecimal amount
) {}
