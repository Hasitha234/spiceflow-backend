package com.spiceflow.backend.workflow;

import java.util.Objects;

/**
 * Represents a valid state transition in a finite state machine.
 * Can be used by aggregates or validators to define allowed state graphs.
 *
 * @param <S> The workflow state type
 */
public record WorkflowTransition<S extends WorkflowState>(
    S fromState,
    S toState
) {
    public WorkflowTransition {
        Objects.requireNonNull(fromState, "fromState must not be null");
        Objects.requireNonNull(toState, "toState must not be null");
    }

    public static <S extends WorkflowState> WorkflowTransition<S> of(S from, S to) {
        return new WorkflowTransition<>(from, to);
    }
}
