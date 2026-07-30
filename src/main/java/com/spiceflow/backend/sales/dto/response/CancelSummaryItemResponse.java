package com.spiceflow.backend.sales.dto.response;

import java.math.BigDecimal;

public record CancelSummaryItemResponse(
    Long id,
    Long productId,
    String productName,
    String productSku,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal estimateValue
) {}
