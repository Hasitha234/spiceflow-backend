package com.spiceflow.backend.inventory.dto.response;

import com.spiceflow.backend.inventory.ledger.InventoryMovementType;
import java.math.BigDecimal;
import java.time.Instant;

public record TransferHistoryResponse(
    Long id,
    Long warehouseId,
    String warehouseName,
    Long productId,
    String productName,
    String productSku,
    InventoryMovementType movementType,
    BigDecimal quantity,
    String referenceId,
    String performedBy,
    Instant timestamp
) {}
