package com.spiceflow.backend.dashboard.inventory.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record RecentMovementDto(
    Long id,
    String movementType,
    Long productId,
    String productName,
    BigDecimal quantity,
    BigDecimal totalValue,
    String referenceId,
    Instant timestamp,
    String performedBy
) {}
