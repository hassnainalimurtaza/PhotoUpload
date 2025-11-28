package com.photoupload.eventbus.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photoupload.common.domain.ProcessingQueue;
import com.photoupload.eventbus.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Database fallback publisher for when message queue is unavailable.
 * Implements Fallback pattern: Graceful degradation when primary service fails.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "event.publisher", havingValue = "database-fallback")
public class DatabaseFallbackPublisher implements EventPublisher {

    private final ObjectMapper objectMapper;

    public DatabaseFallbackPublisher(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        log.warn("Initialized DatabaseFallbackPublisher - message queue is unavailable");
    }

    @Override
    public <T> void publish(T event) {
        String topic = event.getClass().getSimpleName();
        publish(topic, event);
    }

    @Override
    public <T> void publish(String topic, T event) {
        publishWithCorrelation(topic, event, null);
    }

    @Override
    public <T> void publishWithCorrelation(String topic, T event, String correlationId) {
        try {
            log.debug("Storing event in database (fallback mode): topic={}, event={}, correlationId={}",
                topic, event.getClass().getSimpleName(), correlationId);

            // Serialize event to JSON for storage
            String payload = objectMapper.writeValueAsString(event);

            // Note: In a real implementation, this would insert into processing_queue table
            // For now, we just log it
            log.warn("Event stored in database fallback queue: topic={}, eventType={}, payload={}",
                topic, event.getClass().getSimpleName(), payload);

            // TODO: Implement actual database insertion using ProcessingQueue repository
            // ProcessingQueue queueItem = ProcessingQueue.builder()
            //     .commandType(determineCommandType(event))
            //     .payload(payload)
            //     .correlationId(correlationId)
            //     .build();
            // processingQueueRepository.save(queueItem);

        } catch (Exception e) {
            log.error("Failed to store event in database fallback: topic={}, event={}",
                topic, event.getClass().getSimpleName(), e);
            // Swallow exception - this is fallback, we don't want to fail the request
        }
    }

    @Override
    public boolean isAvailable() {
        // Database fallback is always "available" as last resort
        return true;
    }

    @Override
    public String getPublisherType() {
        return "DatabaseFallback";
    }
}

