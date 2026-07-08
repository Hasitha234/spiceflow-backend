package com.spiceflow.backend.dashboard.inventory.dto;

import java.math.BigDecimal;

public record WarehouseStockDto(
    Long warehouseId,
    String warehouseName,
    String location,
    BigDecimal totalValue,
    long itemCount
) {}
