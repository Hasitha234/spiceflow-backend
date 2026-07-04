package com.spiceflow.backend.dashboard.sales.dto;

import java.math.BigDecimal;

public record RecentRepOrderDto(
    Long id,
    String orderNumber,
    Long repId,
    String repName,
    String status,
    String orderDate,
    BigDecimal totalAmount,
    int shopCount
) {}
