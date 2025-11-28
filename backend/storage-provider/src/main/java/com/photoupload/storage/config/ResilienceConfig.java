package com.photoupload.storage.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Resilience4j configuration for Circuit Breaker and Retry patterns.
 * Implements resilience requirements from specification.
 */
@Slf4j
@Configuration
public class ResilienceConfig {

    /**
     * Circuit Breaker Registry with custom configurations
     */
    @Bean
    public CircuitBreakerRegistry circuitBreakerRegistry() {
        // Storage circuit breaker: threshold 50%, slow call duration 2s
        CircuitBreakerConfig storageConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(50)
            .slowCallRateThreshold(50)
            .slowCallDurationThreshold(Duration.ofSeconds(2))
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .permittedNumberOfCallsInHalfOpenState(5)
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .build();

        // Event publisher circuit breaker: threshold 70%, wait duration 30s
        CircuitBreakerConfig eventConfig = CircuitBreakerConfig.custom()
            .failureRateThreshold(70)
            .slowCallRateThreshold(70)
            .slowCallDurationThreshold(Duration.ofSeconds(3))
            .waitDurationInOpenState(Duration.ofSeconds(30))
            .permittedNumberOfCallsInHalfOpenState(3)
            .slidingWindowSize(10)
            .minimumNumberOfCalls(5)
            .build();

        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();

        // Register storage circuit breakers
        registry.circuitBreaker("storage-s3", storageConfig);
        registry.circuitBreaker("storage-gcs", storageConfig);
        registry.circuitBreaker("storage-azure", storageConfig);

        // Register event circuit breaker
        registry.circuitBreaker("event-publisher", eventConfig);

        // Add event listeners for monitoring
        registry.circuitBreaker("storage-s3")
            .getEventPublisher()
            .onStateTransition(event ->
                log.warn("S3 Circuit Breaker state transition: {} -> {}",
                    event.getStateTransition().getFromState(),
                    event.getStateTransition().getToState())
            )
            .onFailureRateExceeded(event ->
                log.error("S3 Circuit Breaker failure rate exceeded: {}%", event.getFailureRate())
            );

        log.info("Configured Circuit Breaker Registry with storage and event configs");
        return registry;
    }

    /**
     * Retry Registry with exponential backoff
     */
    @Bean
    public RetryRegistry retryRegistry() {
        // Exponential backoff: 1s, 2s, 4s
        RetryConfig retryConfig = RetryConfig.custom()
            .maxAttempts(3)
            .intervalFunction(attempt -> (long) Math.pow(2, attempt - 1) * 1000)
            .retryExceptions(Exception.class)
            .build();

        RetryRegistry registry = RetryRegistry.of(retryConfig);

        // Add event listeners
        registry.retry("storage-s3")
            .getEventPublisher()
            .onRetry(event ->
                log.warn("Retrying S3 operation (attempt {}): {}",
                    event.getNumberOfRetryAttempts(),
                    event.getLastThrowable().getMessage())
            );

        log.info("Configured Retry Registry with exponential backoff");
        return registry;
    }
}

