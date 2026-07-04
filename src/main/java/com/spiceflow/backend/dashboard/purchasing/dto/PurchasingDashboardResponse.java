package com.spiceflow.backend.dashboard.purchasing.dto;

import java.math.BigDecimal;
import java.util.List;

public record PurchasingDashboardResponse(
    long totalOpenOrders,
    BigDecimal totalOpenOrderValue,
    BigDecimal totalReceivedMonthValue,
    double averageSupplierLeadTimeDays,
    List<AgingBucketDto> agingBuckets,
    List<SupplierLeadTimeDto> supplierLeadTimes,
    List<OpenPurchaseOrderProjection> recentOpenOrders
) {}
