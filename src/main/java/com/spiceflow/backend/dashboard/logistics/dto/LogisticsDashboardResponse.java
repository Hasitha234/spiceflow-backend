package com.spiceflow.backend.dashboard.logistics.dto;

import java.util.List;

public record LogisticsDashboardResponse(
    long activeLoadingSheetsCount,
    long inProgressDeliveriesCount,
    long completedDeliveriesToday,
    long totalReturnItemsToday,
    List<ActiveLoadingSheetDto> activeLoadingSheets,
    List<InProgressDeliveryDto> inProgressDeliveries
) {}
