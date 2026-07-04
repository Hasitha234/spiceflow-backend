package com.spiceflow.backend.sales.collection.domain;

import com.spiceflow.backend.events.DomainEvent;
import com.spiceflow.backend.events.DomainEventType;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable domain event emitted during Cash Collection workflow transitions.
 */
public record CashCollectionEvent(
        String collectionNumber,
        DomainEventType eventType,
        String correlationId,
        Instant timestamp,
        Long tenantId
) implements DomainEvent {
    public CashCollectionEvent {
        Objects.requireNonNull(collectionNumber, "collectionNumber cannot be null");
        Objects.requireNonNull(eventType, "eventType cannot be null");
        Objects.requireNonNull(correlationId, "correlationId cannot be null");
        Objects.requireNonNull(timestamp, "timestamp cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
    }

    @Override
    public String getAggregateId() {
        return collectionNumber;
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
