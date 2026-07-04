package com.spiceflow.backend.workflow;

import java.time.Instant;
import java.util.Objects;

/**
 * Immutable execution context that flows through all workflow commands, events, audit projections, and logs.
 */
public record WorkflowContext(
    Long userId,
    Long tenantId,
    String correlationId,
    Instant timestamp
) {
    public WorkflowContext {
        Objects.requireNonNull(userId, "userId must not be null");
        Objects.requireNonNull(tenantId, "tenantId must not be null");
        Objects.requireNonNull(correlationId, "correlationId must not be null");
        Objects.requireNonNull(timestamp, "timestamp must not be null");
    }

    public static WorkflowContext of(Long userId, Long tenantId, String correlationId) {
        return new WorkflowContext(userId, tenantId, correlationId, Instant.now());
    }
}
