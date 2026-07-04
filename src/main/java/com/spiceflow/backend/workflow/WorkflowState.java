package com.spiceflow.backend.workflow;

/**
 * Marker interface for all finite state machine (FSM) states in operational workflows.
 * Typically implemented by domain enums (e.g., PurchaseOrderStatus, DeliveryStatus).
 */
public interface WorkflowState {
    String name();
}
