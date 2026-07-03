package com.spiceflow.backend.sales.dto.response;
import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record RepPerformanceResponse(
    Long repId,
    String repName,
    Integer totalOrders,
    BigDecimal totalSalesValue,
    BigDecimal totalCollectedAmount,
    @org.jspecify.annotations.Nullable
    BigDecimal performanceScore // Optional metric based on target vs actual
) {}