package com.spiceflow.backend.sales.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Builder
public class FinalBalanceResponse {
    private Long id;
    private Long repId;
    private String repName;
    @org.jspecify.annotations.Nullable
    private Long driverId;
    @org.jspecify.annotations.Nullable
    private String driverName;
    private LocalDate balanceDate;
    
    private BigDecimal morningSummaryValue;
    private BigDecimal cancelSummaryValue;
    private BigDecimal totalBillCollections;
    private BigDecimal mismatchValue;
    
    private String status;
    @org.jspecify.annotations.Nullable
    private String remarks;
    
    private OffsetDateTime createdAt;
}
