package com.spiceflow.backend.events;

/**
 * Decoupled interface for publishing domain events.
 * Allows migration from synchronous Spring ApplicationEventPublisher to Kafka/RabbitMQ/Outbox without changing domain logic.
 */
public interface DomainEventPublisher {
    void publish(DomainEvent event);
}
