package com.spiceflow.backend.events;

import java.time.Instant;

/**
 * Base interface for all immutable domain events emitted by operational workflows.
 * An event represents something that already happened in the past.
 */
public interface DomainEvent {
    /**
     * Unique identifier of the aggregate that generated this event.
     */
    String getAggregateId();

    /**
     * The type of the domain event.
     */
    DomainEventType getEventType();

    /**
     * The workflow correlation ID linking this event to a broader business transaction.
     */
    String getCorrelationId();

    /**
     * Timestamp when the event occurred.
     */
    Instant getTimestamp();

    /**
     * Tenant ID associated with the event.
     */
    Long getTenantId();
}
