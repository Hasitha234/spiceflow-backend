package com.spiceflow.backend.workflow;

import java.util.Set;
import com.spiceflow.backend.common.exception.BusinessRuleViolationException;

/**
 * Helper interface for validating whether a state transition is permitted in a finite state machine.
 *
 * @param <T> The aggregate type
 * @param <S> The workflow state type
 */
public interface WorkflowValidator<T extends WorkflowAggregate<S>, S extends WorkflowState> {
    /**
     * Returns the set of allowed transitions for this state machine.
     */
    Set<WorkflowTransition<S>> getAllowedTransitions();

    /**
     * Validates that transitioning from aggregate's current state to targetState is allowed.
     * Throws BusinessRuleViolationException if the transition is forbidden.
     */
    default void validateTransition(T aggregate, S targetState) {
        S current = aggregate.getWorkflowState();
        WorkflowTransition<S> transition = WorkflowTransition.of(current, targetState);
        if (!getAllowedTransitions().contains(transition)) {
            throw new BusinessRuleViolationException(
                String.format("Invalid workflow transition from '%s' to '%s' for aggregate '%s'",
                    current.name(), targetState.name(), aggregate.getAggregateId())
            );
        }
    }
}
