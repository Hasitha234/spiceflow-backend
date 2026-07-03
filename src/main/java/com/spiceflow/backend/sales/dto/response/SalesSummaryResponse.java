package com.spiceflow.backend.sales.dto.response;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record SalesSummaryResponse(

    LocalDate startDate,
    LocalDate endDate,
    BigDecimal totalSales,
    BigDecimal totalDiscounts,
    BigDecimal totalReturns,
    BigDecimal netSales,
    BigDecimal totalCollected,
    BigDecimal totalCreditGiven


) {}