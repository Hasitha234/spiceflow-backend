package com.spiceflow.backend.sales.loading.domain;

import com.spiceflow.backend.workflow.WorkflowState;

/**
 * Finite State Machine (FSM) states for Van Loading Sheets.
 * Follows ADR-013 operational workflow lifecycle.
 */
public enum LoadingSheetState implements WorkflowState {
    DRAFT,
    CONFIRMED,
    CANCELLED
}
