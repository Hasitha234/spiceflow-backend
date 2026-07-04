package com.spiceflow.backend.dashboard.inventory.dto;

import java.math.BigDecimal;

public record LowStockItemDto(
    Long productId,
    String sku,
    String name,
    int quantityAvailable,
    String unitOfMeasure,
    BigDecimal basePrice
) {}
