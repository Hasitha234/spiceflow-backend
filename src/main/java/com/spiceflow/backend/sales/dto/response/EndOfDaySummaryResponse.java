package com.spiceflow.backend.sales.dto.response;

import lombok.Builder;
import org.jspecify.annotations.Nullable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
public record EndOfDaySummaryResponse(
    LocalDate date,
    BigDecimal totalSalesValue,
    BigDecimal totalCashCollected,
    BigDecimal totalChequeAmount,
    BigDecimal totalLoanGiven,
    BigDecimal totalReturnsValue,
    BigDecimal totalDiscounts,
    int deliveryCount,
    int shopsVisited,
    List<ChequeDetail> chequeDetails,
    List<DeliverySummary> deliveries,
    List<CancelledOrderSummary> cancelledOrders,
    @Nullable BigDecimal morningSummaryTotal,
    @Nullable BigDecimal cancelSummaryTotal,
    @Nullable BigDecimal netDispatchTotal,
    @Nullable BigDecimal billsTotal,
    @Nullable String balanceStatus,
    List<RepOrderBillSummary> repOrderBills,
    int totalRepOrderBillsCount
) {
    @Builder
    public record ChequeDetail(
        String chequeNo,
        String bankName,
        BigDecimal amount,
        String shopName,
        LocalDate chequeDate
    ) {}

    @Builder
    public record DeliverySummary(
        Long deliveryId,
        String driverName,
        String status,
        BigDecimal salesValue,
        BigDecimal collectedAmount,
        int shopCount
    ) {}

    @Builder
    public record CancelledOrderSummary(
        Long loadingSheetId,
        @Nullable Long repOrderId,
        String driverName,
        String repName,
        String reason
    ) {}

    @Builder
    public record RepOrderBillSummary(
        String repName,
        int orderCount,
        BigDecimal totalAmount,
        List<RepOrderBillShop> shops
    ) {}

    @Builder
    public record RepOrderBillShop(
        String shopName,
        String driverName,
        BigDecimal amount,
        String status
    ) {}
}
