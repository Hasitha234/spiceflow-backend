package com.spiceflow.backend.workflow;

import com.spiceflow.backend.audit.AuditEntry;
import com.spiceflow.backend.events.DomainEvent;
import java.util.List;
import java.util.Objects;

/**
 * Immutable result produced by WorkflowEngine.execute().
 * Contains the transitioned aggregate, emitted domain events, and the audit projection entry.
 *
 * @param <T> The aggregate type
 */
public record WorkflowResult<T>(
    T updatedAggregate,
    List<DomainEvent> events,
    AuditEntry auditEntry
) {
    public WorkflowResult {
        Objects.requireNonNull(updatedAggregate, "updatedAggregate must not be null");
        Objects.requireNonNull(events, "events must not be null");
        Objects.requireNonNull(auditEntry, "auditEntry must not be null");
        events = List.copyOf(events); // ensure immutability
    }
}
