package com.spiceflow.backend.dashboard.finance.dto;

import java.math.BigDecimal;
import java.util.List;

public record FinanceDashboardResponse(
    BigDecimal totalReceivables,
    BigDecimal totalPayables,
    BigDecimal netCashFlowMonth,
    BigDecimal totalCollectionsMonth,
    List<ReceivableAgingBucketDto> receivablesAgingBuckets,
    List<RecentFinancialTransactionDto> recentTransactions
) {}
