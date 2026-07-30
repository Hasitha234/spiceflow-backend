package com.spiceflow.backend.sales.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class FinalBalanceCalculationResponse {
    private Long repId;
    @org.jspecify.annotations.Nullable
    private Long driverId;
    private LocalDate balanceDate;
    private BigDecimal morningSummaryValue;
    private BigDecimal cancelSummaryValue;
    private BigDecimal totalBillCollections;
    private BigDecimal mismatchValue;
    @org.jspecify.annotations.Nullable
    private String status; // BALANCED or MISMATCHED
}
