package com.spiceflow.backend.sales.dto.response;

import lombok.Builder;
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
    List<DeliverySummary> deliveries
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
}
