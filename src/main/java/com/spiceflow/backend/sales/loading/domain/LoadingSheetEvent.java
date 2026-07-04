package com.spiceflow.backend.sales.loading.domain;

import com.spiceflow.backend.events.DomainEvent;
import com.spiceflow.backend.events.DomainEventType;
import java.time.Instant;
import java.util.Objects;

/**
 * Immutable domain event emitted during Loading Sheet workflow transitions.
 */
public record LoadingSheetEvent(
        String sheetNumber,
        DomainEventType eventType,
        String correlationId,
        Instant timestamp,
        Long tenantId
) implements DomainEvent {
    public LoadingSheetEvent {
        Objects.requireNonNull(sheetNumber, "sheetNumber cannot be null");
        Objects.requireNonNull(eventType, "eventType cannot be null");
        Objects.requireNonNull(correlationId, "correlationId cannot be null");
        Objects.requireNonNull(timestamp, "timestamp cannot be null");
        Objects.requireNonNull(tenantId, "tenantId cannot be null");
    }

    @Override
    public String getAggregateId() {
        return sheetNumber;
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
