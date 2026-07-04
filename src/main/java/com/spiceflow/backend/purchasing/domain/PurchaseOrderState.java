package com.spiceflow.backend.purchasing.domain;

import com.spiceflow.backend.workflow.WorkflowState;

public enum PurchaseOrderState implements WorkflowState {
    DRAFT,
    SUBMITTED,
    APPROVED,
    ORDERED,
    PARTIALLY_RECEIVED,
    RECEIVED,
    CLOSED,
    REJECTED
}
