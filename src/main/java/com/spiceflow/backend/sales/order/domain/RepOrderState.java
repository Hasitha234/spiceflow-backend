package com.spiceflow.backend.sales.order.domain;

import com.spiceflow.backend.workflow.WorkflowState;

/**
 * Finite State Machine (FSM) states for Sales Representative Orders (Rep Orders).
 * Follows ADR-013 operational workflow lifecycle.
 */
public enum RepOrderState implements WorkflowState {
    DRAFT,
    SUBMITTED,
    APPROVED,
    LOADED,
    DELIVERED,
    CANCELLED
}
