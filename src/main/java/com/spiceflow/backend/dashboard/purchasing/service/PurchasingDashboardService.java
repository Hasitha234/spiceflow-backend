package com.spiceflow.backend.dashboard.purchasing.service;

import com.spiceflow.backend.dashboard.purchasing.dto.AgingBucketDto;
import com.spiceflow.backend.dashboard.purchasing.dto.OpenPurchaseOrderProjection;
import com.spiceflow.backend.dashboard.purchasing.dto.PurchasingDashboardResponse;
import com.spiceflow.backend.dashboard.purchasing.dto.SupplierLeadTimeDto;
import com.spiceflow.backend.dashboard.purchasing.repository.PurchasingDashboardRepository;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pure CQRS Read Model service orchestrating queries for the Purchasing Dashboard.
 * Operates strictly read-only without transaction boundary entanglement or entity mutation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchasingDashboardService {

    private final PurchasingDashboardRepository repository;

    public PurchasingDashboardResponse getDashboard(Long tenantId, int limit) {
        log.debug("Fetching purchasing dashboard projection for tenantId={}, limit={}", tenantId, limit);
        Instant startOfMonth = ZonedDateTime.now(ZoneId.systemDefault())
            .withDayOfMonth(1)
            .toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant();

        PurchasingDashboardRepository.SummaryMetrics metrics = repository.getSummaryMetrics(tenantId, startOfMonth);
        List<AgingBucketDto> agingBuckets = repository.getAgingBuckets(tenantId);
        List<SupplierLeadTimeDto> supplierLeadTimes = repository.getSupplierLeadTimes(tenantId);
        List<OpenPurchaseOrderProjection> recentOpenOrders = repository.getRecentOpenOrders(tenantId, limit);

        return new PurchasingDashboardResponse(
            metrics.totalOpenOrders(),
            metrics.totalOpenOrderValue(),
            metrics.totalReceivedMonthValue(),
            metrics.averageSupplierLeadTimeDays(),
            agingBuckets,
            supplierLeadTimes,
            recentOpenOrders
        );
    }
}
