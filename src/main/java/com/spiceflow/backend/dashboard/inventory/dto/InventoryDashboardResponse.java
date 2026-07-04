package com.spiceflow.backend.dashboard.inventory.dto;

import java.math.BigDecimal;
import java.util.List;

public record InventoryDashboardResponse(
    BigDecimal totalStockValue,
    long totalItemsCount,
    long lowStockCount,
    long pendingTransfersCount,
    List<LowStockItemDto> lowStockItems,
    List<RecentMovementDto> recentMovements
) {}
