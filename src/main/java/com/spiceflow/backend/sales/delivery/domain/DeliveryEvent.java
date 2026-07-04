package com.spiceflow.backend.sales.delivery.domain;

import com.spiceflow.backend.events.DomainEvent;
import com.spiceflow.backend.events.DomainEventType;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable domain event emitted during Delivery workflow transitions.
 */
public record DeliveryEvent(
        String deliveryNumber,
        DomainEventType eventType,
        String correlationId,
        Instant timestamp,
        Long tenantId
) implements DomainEvent {
    public DeliveryEvent {
        Objects.requireNonNull(deliveryNumber, "deliveryNumber cannot be null");
        Objects.requireNonNull(eventType, "eventType cannot be null");
        Objects.requireNonNull(correlationId, "correlationId cannot be null");
        Objects.requireNonNull(timestamp, "timestamp cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
    }

    @Override
    public String getAggregateId() {
        return deliveryNumber;
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
