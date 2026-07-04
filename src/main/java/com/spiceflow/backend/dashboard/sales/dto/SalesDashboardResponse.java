package com.spiceflow.backend.dashboard.sales.dto;

import java.math.BigDecimal;
import java.util.List;

public record SalesDashboardResponse(
    BigDecimal todaySalesValue,
    BigDecimal monthSalesValue,
    BigDecimal monthCollectionsValue,
    BigDecimal totalOutstandingLoans,
    List<RecentRepOrderDto> recentOrders,
    List<TopDebtorShopDto> topDebtorShops
) {}
