package com.spiceflow.backend.sales.collection.domain;

import com.spiceflow.backend.workflow.WorkflowState;

/**
 * Finite State Machine (FSM) states for Cash Collection workflow.
 * Follows ADR-013 operational workflow lifecycle.
 */
public enum CashCollectionState implements WorkflowState {
    PENDING,
    CONFIRMED,
    CANCELLED
}
