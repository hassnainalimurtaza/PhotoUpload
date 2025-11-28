package com.photoupload.eventbus.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration with Dead Letter Queue (DLQ) setup.
 * Implements resilience patterns: DLQ, TTL, retry mechanism.
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "event.publisher", havingValue = "rabbitmq", matchIfMissing = true)
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "photo.events";
    public static final String DLQ_EXCHANGE_NAME = "photo.events.dlq";

    // Queue names
    public static final String PHOTO_UPLOADED_QUEUE = "photo.uploaded";
    public static final String PHOTO_PROCESSING_STARTED_QUEUE = "photo.processing.started";
    public static final String PHOTO_PROCESSING_COMPLETED_QUEUE = "photo.processing.completed";
    public static final String PHOTO_PROCESSING_FAILED_QUEUE = "photo.processing.failed";
    public static final String PHOTO_DELETED_QUEUE = "photo.deleted";

    // DLQ names
    public static final String PHOTO_UPLOADED_DLQ = "photo.uploaded.dlq";
    public static final String PHOTO_PROCESSING_STARTED_DLQ = "photo.processing.started.dlq";
    public static final String PHOTO_PROCESSING_COMPLETED_DLQ = "photo.processing.completed.dlq";
    public static final String PHOTO_PROCESSING_FAILED_DLQ = "photo.processing.failed.dlq";
    public static final String PHOTO_DELETED_DLQ = "photo.deleted.dlq";

    /**
     * Main exchange for photo events
     */
    @Bean
    public TopicExchange photoEventsExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    /**
     * Dead Letter Queue exchange
     */
    @Bean
    public DirectExchange dlqExchange() {
        return new DirectExchange(DLQ_EXCHANGE_NAME, true, false);
    }

    /**
     * Message converter for JSON serialization
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate with JSON converter
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    /**
     * Listener container factory with retry configuration
     */
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
        ConnectionFactory connectionFactory,
        MessageConverter messageConverter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        factory.setDefaultRequeueRejected(false); // Send to DLQ on failure
        factory.setPrefetchCount(10);
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }

    // ========== Photo Uploaded Queue ==========

    @Bean
    public Queue photoUploadedQueue() {
        return QueueBuilder.durable(PHOTO_UPLOADED_QUEUE)
            .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE_NAME)
            .withArgument("x-dead-letter-routing-key", PHOTO_UPLOADED_DLQ)
            .withArgument("x-message-ttl", 1800000) // 30 minutes
            .build();
    }

    @Bean
    public Queue photoUploadedDLQ() {
        return new Queue(PHOTO_UPLOADED_DLQ, true);
    }

    @Bean
    public Binding photoUploadedBinding() {
        return BindingBuilder.bind(photoUploadedQueue())
            .to(photoEventsExchange())
            .with("PhotoUploadedEvent");
    }

    @Bean
    public Binding photoUploadedDLQBinding() {
        return BindingBuilder.bind(photoUploadedDLQ())
            .to(dlqExchange())
            .with(PHOTO_UPLOADED_DLQ);
    }

    // ========== Photo Processing Started Queue ==========

    @Bean
    public Queue photoProcessingStartedQueue() {
        return QueueBuilder.durable(PHOTO_PROCESSING_STARTED_QUEUE)
            .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE_NAME)
            .withArgument("x-dead-letter-routing-key", PHOTO_PROCESSING_STARTED_DLQ)
            .withArgument("x-message-ttl", 1800000)
            .build();
    }

    @Bean
    public Queue photoProcessingStartedDLQ() {
        return new Queue(PHOTO_PROCESSING_STARTED_DLQ, true);
    }

    @Bean
    public Binding photoProcessingStartedBinding() {
        return BindingBuilder.bind(photoProcessingStartedQueue())
            .to(photoEventsExchange())
            .with("PhotoProcessingStartedEvent");
    }

    @Bean
    public Binding photoProcessingStartedDLQBinding() {
        return BindingBuilder.bind(photoProcessingStartedDLQ())
            .to(dlqExchange())
            .with(PHOTO_PROCESSING_STARTED_DLQ);
    }

    // ========== Photo Processing Completed Queue ==========

    @Bean
    public Queue photoProcessingCompletedQueue() {
        return QueueBuilder.durable(PHOTO_PROCESSING_COMPLETED_QUEUE)
            .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE_NAME)
            .withArgument("x-dead-letter-routing-key", PHOTO_PROCESSING_COMPLETED_DLQ)
            .withArgument("x-message-ttl", 1800000)
            .build();
    }

    @Bean
    public Queue photoProcessingCompletedDLQ() {
        return new Queue(PHOTO_PROCESSING_COMPLETED_DLQ, true);
    }

    @Bean
    public Binding photoProcessingCompletedBinding() {
        return BindingBuilder.bind(photoProcessingCompletedQueue())
            .to(photoEventsExchange())
            .with("PhotoProcessingCompletedEvent");
    }

    @Bean
    public Binding photoProcessingCompletedDLQBinding() {
        return BindingBuilder.bind(photoProcessingCompletedDLQ())
            .to(dlqExchange())
            .with(PHOTO_PROCESSING_COMPLETED_DLQ);
    }

    // ========== Photo Processing Failed Queue ==========

    @Bean
    public Queue photoProcessingFailedQueue() {
        return QueueBuilder.durable(PHOTO_PROCESSING_FAILED_QUEUE)
            .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE_NAME)
            .withArgument("x-dead-letter-routing-key", PHOTO_PROCESSING_FAILED_DLQ)
            .withArgument("x-message-ttl", 1800000)
            .build();
    }

    @Bean
    public Queue photoProcessingFailedDLQ() {
        return new Queue(PHOTO_PROCESSING_FAILED_DLQ, true);
    }

    @Bean
    public Binding photoProcessingFailedBinding() {
        return BindingBuilder.bind(photoProcessingFailedQueue())
            .to(photoEventsExchange())
            .with("PhotoProcessingFailedEvent");
    }

    @Bean
    public Binding photoProcessingFailedDLQBinding() {
        return BindingBuilder.bind(photoProcessingFailedDLQ())
            .to(dlqExchange())
            .with(PHOTO_PROCESSING_FAILED_DLQ);
    }

    // ========== Photo Deleted Queue ==========

    @Bean
    public Queue photoDeletedQueue() {
        return QueueBuilder.durable(PHOTO_DELETED_QUEUE)
            .withArgument("x-dead-letter-exchange", DLQ_EXCHANGE_NAME)
            .withArgument("x-dead-letter-routing-key", PHOTO_DELETED_DLQ)
            .withArgument("x-message-ttl", 1800000)
            .build();
    }

    @Bean
    public Queue photoDeletedDLQ() {
        return new Queue(PHOTO_DELETED_DLQ, true);
    }

    @Bean
    public Binding photoDeletedBinding() {
        return BindingBuilder.bind(photoDeletedQueue())
            .to(photoEventsExchange())
            .with("PhotoDeletedEvent");
    }

    @Bean
    public Binding photoDeletedDLQBinding() {
        return BindingBuilder.bind(photoDeletedDLQ())
            .to(dlqExchange())
            .with(PHOTO_DELETED_DLQ);
    }
}

