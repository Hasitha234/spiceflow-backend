package com.spiceflow.backend.inventory.transfer.domain;

import com.spiceflow.backend.workflow.WorkflowState;

/**
 * Finite state machine states for the Warehouse Transfer operational workflow.
 */
public enum WarehouseTransferState implements WorkflowState {
    DRAFT,
    REQUESTED,
    APPROVED,
    SHIPPED,
    RECEIVED,
    CANCELLED
}
