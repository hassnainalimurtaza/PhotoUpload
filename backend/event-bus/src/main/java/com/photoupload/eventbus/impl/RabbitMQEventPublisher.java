package com.photoupload.eventbus.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.photoupload.common.exception.EventPublishException;
import com.photoupload.eventbus.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * RabbitMQ implementation of EventPublisher.
 * Implements Strategy pattern for RabbitMQ-specific event publishing.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "event.publisher", havingValue = "rabbitmq", matchIfMissing = true)
public class RabbitMQEventPublisher implements EventPublisher {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;
    private final String exchangeName;

    public RabbitMQEventPublisher(
        RabbitTemplate rabbitTemplate,
        ObjectMapper objectMapper
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.objectMapper = objectMapper;
        this.exchangeName = "photo.events";
        log.info("Initialized RabbitMQEventPublisher with exchange: {}", exchangeName);
    }

    @Override
    public <T> void publish(T event) {
        String routingKey = event.getClass().getSimpleName();
        publish(routingKey, event);
    }

    @Override
    public <T> void publish(String topic, T event) {
        publishWithCorrelation(topic, event, null);
    }

    @Override
    public <T> void publishWithCorrelation(String topic, T event, String correlationId) {
        try {
            log.debug("Publishing event to RabbitMQ: topic={}, event={}, correlationId={}",
                topic, event.getClass().getSimpleName(), correlationId);

            // Serialize event to JSON
            byte[] body = objectMapper.writeValueAsBytes(event);

            // Build message with properties
            MessageProperties properties = new MessageProperties();
            properties.setContentType("application/json");
            properties.setHeader("eventType", event.getClass().getSimpleName());
            
            if (correlationId != null) {
                properties.setCorrelationId(correlationId);
            }

            Message message = MessageBuilder
                .withBody(body)
                .andProperties(properties)
                .build();

            // Publish to exchange with routing key
            rabbitTemplate.send(exchangeName, topic, message);

            log.info("Successfully published event to RabbitMQ: topic={}, eventType={}",
                topic, event.getClass().getSimpleName());

        } catch (AmqpException e) {
            log.error("Failed to publish event to RabbitMQ: topic={}, event={}",
                topic, event.getClass().getSimpleName(), e);
            throw new EventPublishException(event.getClass().getSimpleName(), topic, e.getMessage(), e);
        } catch (Exception e) {
            log.error("Unexpected error publishing event to RabbitMQ: topic={}, event={}",
                topic, event.getClass().getSimpleName(), e);
            throw new EventPublishException(event.getClass().getSimpleName(), "Unexpected error: " + e.getMessage());
        }
    }

    @Override
    public boolean isAvailable() {
        try {
            rabbitTemplate.execute(channel -> {
                channel.queueDeclarePassive("health-check-queue");
                return true;
            });
            return true;
        } catch (Exception e) {
            log.warn("RabbitMQ is not available: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getPublisherType() {
        return "RabbitMQ";
    }
}

