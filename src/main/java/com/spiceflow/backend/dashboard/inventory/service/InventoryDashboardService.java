package com.spiceflow.backend.dashboard.inventory.service;

import com.spiceflow.backend.dashboard.inventory.dto.InventoryDashboardResponse;
import com.spiceflow.backend.dashboard.inventory.dto.LowStockItemDto;
import com.spiceflow.backend.dashboard.inventory.dto.RecentMovementDto;
import com.spiceflow.backend.dashboard.inventory.repository.InventoryDashboardRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pure CQRS Read Model service orchestrating queries for the Inventory Dashboard.
 * Operates strictly read-only without transaction boundary entanglement or entity mutation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventoryDashboardService {

    private final InventoryDashboardRepository repository;

    public InventoryDashboardResponse getDashboard(Long tenantId, int limit) {
        log.debug("Fetching inventory dashboard projection for tenantId={}, limit={}", tenantId, limit);

        InventoryDashboardRepository.SummaryMetrics metrics = repository.getSummaryMetrics(tenantId);
        List<LowStockItemDto> lowStockItems = repository.getLowStockItems(tenantId, limit);
        List<RecentMovementDto> recentMovements = repository.getRecentMovements(tenantId, limit);
        List<com.spiceflow.backend.dashboard.inventory.dto.WarehouseStockDto> warehouseStocks = repository.getWarehouseStocks(tenantId);

        return new InventoryDashboardResponse(
            metrics != null && metrics.totalStockValue() != null ? metrics.totalStockValue() : java.math.BigDecimal.ZERO,
            metrics != null ? metrics.totalItemsCount() : 0L,
            metrics != null ? metrics.lowStockCount() : 0L,
            metrics != null ? metrics.pendingTransfersCount() : 0L,
            lowStockItems,
            recentMovements,
            warehouseStocks
        );
    }
}
