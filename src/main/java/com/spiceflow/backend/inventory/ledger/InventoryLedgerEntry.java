package com.spiceflow.backend.inventory.ledger;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import org.jspecify.annotations.Nullable;

/**
 * Immutable domain record representing a double-entry stock movement in the ledger.
 */
public record InventoryLedgerEntry(
        @Nullable Long id,
        Long tenantId,
        Long warehouseId,
        Long productId,
        InventoryMovementType movementType,
        BigDecimal quantity,
        BigDecimal unitCost,
        BigDecimal totalValue,
        String referenceId,
        String lotNumber,
        @Nullable LocalDate expirationDate,
        Instant timestamp,
        String performedBy
) {
    public static InventoryLedgerEntry createReceipt(Long tenantId, Long warehouseId, Long productId,
                                                     BigDecimal quantity, BigDecimal unitCost,
                                                     String referenceId, String lotNumber,
                                                     @Nullable LocalDate expirationDate,
                                                     Instant timestamp, String performedBy) {
        BigDecimal totalVal = quantity.multiply(unitCost);
        return new InventoryLedgerEntry(
                null, tenantId, warehouseId, productId,
                InventoryMovementType.RECEIPT, quantity, unitCost, totalVal,
                referenceId, lotNumber != null ? lotNumber : "", expirationDate,
                timestamp, performedBy
        );
    }
}
