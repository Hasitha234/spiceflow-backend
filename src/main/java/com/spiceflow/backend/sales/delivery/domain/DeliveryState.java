package com.spiceflow.backend.sales.delivery.domain;

import com.spiceflow.backend.workflow.WorkflowState;

/**
 * Finite State Machine (FSM) states for Customer Deliveries.
 * Follows ADR-013 operational workflow lifecycle.
 */
public enum DeliveryState implements WorkflowState {
    IN_PROGRESS,
    DISPATCHED,
    COMPLETED,
    CANCELLED
}
