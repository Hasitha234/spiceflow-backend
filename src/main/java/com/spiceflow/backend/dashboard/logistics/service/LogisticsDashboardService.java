package com.spiceflow.backend.dashboard.logistics.service;

import com.spiceflow.backend.dashboard.logistics.dto.ActiveLoadingSheetDto;
import com.spiceflow.backend.dashboard.logistics.dto.InProgressDeliveryDto;
import com.spiceflow.backend.dashboard.logistics.dto.LogisticsDashboardResponse;
import com.spiceflow.backend.dashboard.logistics.repository.LogisticsDashboardRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pure CQRS Read Model service orchestrating queries for the Logistics Dashboard.
 * Operates strictly read-only without transaction boundary entanglement or entity mutation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LogisticsDashboardService {

    private final LogisticsDashboardRepository repository;

    public LogisticsDashboardResponse getDashboard(Long tenantId, int limit) {
        log.debug("Fetching logistics dashboard projection for tenantId={}, limit={}", tenantId, limit);

        LogisticsDashboardRepository.SummaryMetrics metrics = repository.getSummaryMetrics(tenantId);
        List<ActiveLoadingSheetDto> activeLoadingSheets = repository.getActiveLoadingSheets(tenantId, limit);
        List<InProgressDeliveryDto> inProgressDeliveries = repository.getInProgressDeliveries(tenantId, limit);

        return new LogisticsDashboardResponse(
            metrics.activeLoadingSheetsCount(),
            metrics.inProgressDeliveriesCount(),
            metrics.completedDeliveriesToday(),
            metrics.totalReturnItemsToday(),
            activeLoadingSheets,
            inProgressDeliveries
        );
    }
}
