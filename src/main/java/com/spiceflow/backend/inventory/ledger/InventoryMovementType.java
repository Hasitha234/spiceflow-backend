package com.spiceflow.backend.inventory.ledger;

/**
 * Types of immutable inventory movements in the double-entry stock ledger.
 */
public enum InventoryMovementType {
    RECEIPT,
    DELIVERY,
    TRANSFER_IN,
    TRANSFER_OUT,
    ADJUSTMENT_IN,
    ADJUSTMENT_OUT,
    DAMAGED,
    INITIAL_STOCK,
    MORNING_DISPATCH,
    MORNING_DISPATCH_REVERSAL
}
