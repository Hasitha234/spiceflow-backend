package com.spiceflow.backend.dashboard.sales.service;

import com.spiceflow.backend.dashboard.sales.dto.RecentRepOrderDto;
import com.spiceflow.backend.dashboard.sales.dto.SalesDashboardResponse;
import com.spiceflow.backend.dashboard.sales.dto.TopDebtorShopDto;
import com.spiceflow.backend.dashboard.sales.repository.SalesDashboardRepository;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pure CQRS Read Model service orchestrating queries for the Sales Dashboard.
 * Operates strictly read-only without transaction boundary entanglement or entity mutation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesDashboardService {

    private final SalesDashboardRepository repository;

    public SalesDashboardResponse getDashboard(Long tenantId, int limit) {
        log.debug("Fetching sales dashboard projection for tenantId={}, limit={}", tenantId, limit);

        Instant startOfMonth = ZonedDateTime.now(ZoneOffset.UTC)
            .withDayOfMonth(1)
            .toLocalDate()
            .atStartOfDay(ZoneOffset.UTC)
            .toInstant();

        SalesDashboardRepository.SummaryMetrics metrics = repository.getSummaryMetrics(tenantId, startOfMonth);
        List<RecentRepOrderDto> recentOrders = repository.getRecentOrders(tenantId, limit);
        List<TopDebtorShopDto> topDebtorShops = repository.getTopDebtorShops(tenantId, limit);

        return new SalesDashboardResponse(
            metrics != null && metrics.todaySalesValue() != null ? metrics.todaySalesValue() : java.math.BigDecimal.ZERO,
            metrics != null && metrics.monthSalesValue() != null ? metrics.monthSalesValue() : java.math.BigDecimal.ZERO,
            metrics != null && metrics.monthCollectionsValue() != null ? metrics.monthCollectionsValue() : java.math.BigDecimal.ZERO,
            metrics != null && metrics.totalOutstandingLoans() != null ? metrics.totalOutstandingLoans() : java.math.BigDecimal.ZERO,
            recentOrders,
            topDebtorShops
        );
    }
}
