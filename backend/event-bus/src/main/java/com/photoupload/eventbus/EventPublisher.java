package com.photoupload.eventbus;

/**
 * Interface Segregation Principle: Focused interface for event publishing.
 * Strategy Pattern: Multiple implementations (RabbitMQ, Kafka, Database fallback).
 */
public interface EventPublisher {

    /**
     * Publish event to message queue
     *
     * @param event Event object to publish
     * @param <T>   Event type
     */
    <T> void publish(T event);

    /**
     * Publish event to specific topic/queue
     *
     * @param topic Topic or queue name
     * @param event Event object
     * @param <T>   Event type
     */
    <T> void publish(String topic, T event);

    /**
     * Publish event with correlation ID for tracing
     *
     * @param topic         Topic or queue name
     * @param event         Event object
     * @param correlationId Correlation ID for request tracing
     * @param <T>           Event type
     */
    <T> void publishWithCorrelation(String topic, T event, String correlationId);

    /**
     * Check if event bus is available
     *
     * @return true if available
     */
    boolean isAvailable();

    /**
     * Get publisher type
     *
     * @return Publisher identifier (RabbitMQ, Kafka, Database)
     */
    String getPublisherType();
}

