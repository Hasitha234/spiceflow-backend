package com.spiceflow.backend.sales.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class SalesSummaryResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal totalSales;
    private BigDecimal totalDiscounts;
    private BigDecimal totalReturns;
    private BigDecimal netSales;
    private BigDecimal totalCollected;
    private BigDecimal totalCreditGiven;
}
