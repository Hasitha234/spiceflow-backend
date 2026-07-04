package com.spiceflow.backend.workflow;

import com.spiceflow.backend.events.DomainEvent;
import java.util.List;

/**
 * Immutable container for the result of an aggregate state transition.
 * Holds the new copy-on-write aggregate instance and any generated domain events.
 */
public record WorkflowTransitionOutput<T>(
        T updatedAggregate,
        List<DomainEvent> events
) {
    public WorkflowTransitionOutput {
        events = List.copyOf(events);
    }
}
