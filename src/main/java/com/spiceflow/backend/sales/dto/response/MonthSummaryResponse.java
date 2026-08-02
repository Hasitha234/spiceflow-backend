package com.spiceflow.backend.sales.dto.response;

import lombok.Builder;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

@Builder
public record MonthSummaryResponse(
    YearMonth yearMonth,
    BigDecimal totalSalesValue,
    BigDecimal totalPurchasesValue,
    BigDecimal totalExpensesValue,
    BigDecimal netProfit,
    int deliveryCount,
    int repOrderCount,
    int purchaseOrderCount,
    List<ExpenseBreakdown> expenseBreakdown,
    @org.jspecify.annotations.Nullable BigDecimal totalMorningDispatch,
    @org.jspecify.annotations.Nullable BigDecimal totalCancelReturns,
    @org.jspecify.annotations.Nullable BigDecimal totalBilledAmount,
    int balancedDaysCount
) {
    @Builder
    public record ExpenseBreakdown(
        String category,
        BigDecimal amount
    ) {}
}
