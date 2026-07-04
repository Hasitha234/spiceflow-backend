package com.spiceflow.backend.sales.order.domain;

import com.spiceflow.backend.events.DomainEvent;
import com.spiceflow.backend.events.DomainEventType;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable domain event emitted during Rep Order workflow transitions.
 */
public record RepOrderEvent(
        String orderNumber,
        DomainEventType eventType,
        String correlationId,
        Instant timestamp,
        Long tenantId
) implements DomainEvent {
    public RepOrderEvent {
        Objects.requireNonNull(orderNumber, "orderNumber cannot be null");
        Objects.requireNonNull(eventType, "eventType cannot be null");
        Objects.requireNonNull(correlationId, "correlationId cannot be null");
        Objects.requireNonNull(timestamp, "timestamp cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
    }

    @Override
    public String getAggregateId() {
        return orderNumber;
    }

    @Override
    public DomainEventType getEventType() {
        return eventType;
    }

    @Override
    public String getCorrelationId() {
        return correlationId;
    }

    @Override
    public Instant getTimestamp() {
        return timestamp;
    }

    @Override
    public Long getTenantId() {
        return tenantId;
    }
}
