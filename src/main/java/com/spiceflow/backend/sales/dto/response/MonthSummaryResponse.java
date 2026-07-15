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
    List<ExpenseBreakdown> expenseBreakdown
) {
    @Builder
    public record ExpenseBreakdown(
        String category,
        BigDecimal amount
    ) {}
}
