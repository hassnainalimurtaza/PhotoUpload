package com.photoupload.storage.decorator;

import com.photoupload.storage.CloudStorageProvider;
import com.photoupload.storage.StorageMetadata;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.time.Duration;
import java.util.function.Supplier;

/**
 * Decorator Pattern: Adds resilience (Circuit Breaker + Retry) to CloudStorageProvider.
 * Wraps any CloudStorageProvider implementation with retry and circuit breaker logic.
 */
@Slf4j
public class ResilientCloudStorageProvider implements CloudStorageProvider {

    private final CloudStorageProvider delegate;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public ResilientCloudStorageProvider(
        CloudStorageProvider delegate,
        CircuitBreaker circuitBreaker,
        Retry retry
    ) {
        this.delegate = delegate;
        this.circuitBreaker = circuitBreaker;
        this.retry = retry;
        log.info("Wrapped {} with resilience patterns (Circuit Breaker + Retry)", delegate.getProviderName());
    }

    @Override
    public String upload(String key, InputStream inputStream, String contentType, long fileSize) {
        Supplier<String> supplier = CircuitBreaker.decorateSupplier(
            circuitBreaker,
            () -> delegate.upload(key, inputStream, contentType, fileSize)
        );
        supplier = Retry.decorateSupplier(retry, supplier);
        return supplier.get();
    }

    @Override
    public InputStream download(String key) {
        Supplier<InputStream> supplier = CircuitBreaker.decorateSupplier(
            circuitBreaker,
            () -> delegate.download(key)
        );
        supplier = Retry.decorateSupplier(retry, supplier);
        return supplier.get();
    }

    @Override
    public boolean delete(String key) {
        Supplier<Boolean> supplier = CircuitBreaker.decorateSupplier(
            circuitBreaker,
            () -> delegate.delete(key)
        );
        supplier = Retry.decorateSupplier(retry, supplier);
        return supplier.get();
    }

    @Override
    public boolean exists(String key) {
        Supplier<Boolean> supplier = CircuitBreaker.decorateSupplier(
            circuitBreaker,
            () -> delegate.exists(key)
        );
        // No retry for exists check - fast fail
        return supplier.get();
    }

    @Override
    public String generatePresignedUrl(String key, Duration duration) {
        Supplier<String> supplier = CircuitBreaker.decorateSupplier(
            circuitBreaker,
            () -> delegate.generatePresignedUrl(key, duration)
        );
        supplier = Retry.decorateSupplier(retry, supplier);
        return supplier.get();
    }

    @Override
    public StorageMetadata getMetadata(String key) {
        Supplier<StorageMetadata> supplier = CircuitBreaker.decorateSupplier(
            circuitBreaker,
            () -> delegate.getMetadata(key)
        );
        supplier = Retry.decorateSupplier(retry, supplier);
        return supplier.get();
    }

    @Override
    public String getProviderName() {
        return delegate.getProviderName() + " (Resilient)";
    }

    /**
     * Get circuit breaker state
     */
    public CircuitBreaker.State getCircuitBreakerState() {
        return circuitBreaker.getState();
    }

    /**
     * Get retry metrics
     */
    public Retry.Metrics getRetryMetrics() {
        return retry.getMetrics();
    }
}

