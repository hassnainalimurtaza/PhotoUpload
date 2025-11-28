package com.photoupload.eventbus.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photoupload.common.exception.EventPublishException;
import com.photoupload.eventbus.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka implementation of EventPublisher.
 * Implements Strategy pattern for Kafka-specific event publishing.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "event.publisher", havingValue = "kafka")
public class KafkaEventPublisher implements EventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topicPrefix;

    public KafkaEventPublisher(
        KafkaTemplate<String, String> kafkaTemplate,
        ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topicPrefix = "photo-events-";
        log.info("Initialized KafkaEventPublisher with topic prefix: {}", topicPrefix);
    }

    @Override
    public <T> void publish(T event) {
        String topic = topicPrefix + event.getClass().getSimpleName();
        publish(topic, event);
    }

    @Override
    public <T> void publish(String topic, T event) {
        publishWithCorrelation(topic, event, null);
    }

    @Override
    public <T> void publishWithCorrelation(String topic, T event, String correlationId) {
        try {
            log.debug("Publishing event to Kafka: topic={}, event={}, correlationId={}",
                topic, event.getClass().getSimpleName(), correlationId);

            // Serialize event to JSON
            String message = objectMapper.writeValueAsString(event);

            // Send message
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, correlationId, message);

            // Add callback for success/failure
            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Failed to publish event to Kafka: topic={}, event={}",
                        topic, event.getClass().getSimpleName(), ex);
                    throw new EventPublishException(event.getClass().getSimpleName(), topic, ex.getMessage(), ex);
                } else {
                    log.info("Successfully published event to Kafka: topic={}, eventType={}, partition={}, offset={}",
                        topic, event.getClass().getSimpleName(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
                }
            });

        } catch (Exception e) {
            log.error("Unexpected error publishing event to Kafka: topic={}, event={}",
                topic, event.getClass().getSimpleName(), e);
            throw new EventPublishException(event.getClass().getSimpleName(), "Unexpected error: " + e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            // Simple check - if we can get metrics, Kafka is available
            kafkaTemplate.metrics();
            return true;
        } catch (Exception e) {
            log.warn("Kafka is not available: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getPublisherType() {
        return "Kafka";
    }
}

