package com.photoupload.common.exception;

/**
 * Exception thrown when event publishing fails
 */
public class EventPublishException extends RuntimeException {

    private final String eventType;
    private final String queueName;

    public EventPublishException(String eventType, String message) {
        super(String.format("Failed to publish event %s: %s", eventType, message));
        this.eventType = eventType;
        this.queueName = null;
    }

    public EventPublishException(String eventType, String queueName, String message, Throwable cause) {
        super(String.format("Failed to publish event %s to queue %s: %s", eventType, queueName, message), cause);
        this.eventType = eventType;
        this.queueName = queueName;
    }

    public String getEventType() {
        return eventType;
    }

    public String getQueueName() {
        return queueName;
    }
}

