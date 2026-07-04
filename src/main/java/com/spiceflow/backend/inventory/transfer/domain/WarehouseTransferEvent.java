package com.spiceflow.backend.inventory.transfer.domain;

import com.spiceflow.backend.events.DomainEvent;
import com.spiceflow.backend.events.DomainEventType;
import java.time.Instant;

public record WarehouseTransferEvent(
        String aggregateId,
        DomainEventType eventType,
        String correlationId,
        Instant timestamp,
        Long tenantId
) implements DomainEvent {
    @Override public String getAggregateId() { return aggregateId; }
    @Override public DomainEventType getEventType() { return eventType; }
    @Override public String getCorrelationId() { return correlationId; }
    @Override public Instant getTimestamp() { return timestamp; }
    @Override public Long getTenantId() { return tenantId; }
}
