package com.spiceflow.backend.receiving.domain;

import com.spiceflow.backend.workflow.WorkflowState;

/**
 * Finite state machine states for the Goods Receipt operational workflow.
 */
public enum GoodsReceiptState implements WorkflowState {
    DRAFT,
    INSPECTING,
    VERIFIED,
    POSTED,
    CANCELLED
}
