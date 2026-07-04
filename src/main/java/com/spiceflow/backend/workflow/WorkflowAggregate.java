package com.spiceflow.backend.workflow;

import com.spiceflow.backend.events.DomainEvent;
import java.util.List;

/**
 * Interface implemented by operational aggregates that undergo finite state machine transitions.
 * Enforces Rule 15 copy-on-write immutability: transitionTo returns a new aggregate instance and events.
 *
 * @param <T> The aggregate type
 * @param <S> The workflow state type
 */
public interface WorkflowAggregate<T extends WorkflowAggregate<T, S>, S extends WorkflowState> {
    /**
     * Unique identifier of the aggregate (e.g., PO ID or string code).
     */
    String getAggregateId();

    /**
     * Returns the current workflow state of the aggregate.
     */
    S getWorkflowState();

    /**
     * Transitions the aggregate to the target state without mutating the original instance (copy-on-write).
     * Called exclusively by WorkflowEngine after validation.
     *
     * @param targetState The state to transition to
     * @param context     The execution context
     * @return Output containing the new updated aggregate instance and generated domain events
     */
    WorkflowTransitionOutput<T> transitionTo(S targetState, WorkflowContext context);
}
