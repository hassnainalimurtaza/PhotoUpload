package com.photoupload.storage.factory;

import com.photoupload.storage.CloudStorageProvider;
import com.photoupload.storage.decorator.ResilientCloudStorageProvider;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Factory Pattern: Creates and manages CloudStorageProvider instances.
 * Implements Single Responsibility Principle: Responsible only for provider creation.
 */
@Slf4j
@Component
public class CloudStorageProviderFactory {

    private final Map<String, CloudStorageProvider> providers;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;

    public CloudStorageProviderFactory(
        List<CloudStorageProvider> providerList,
        CircuitBreakerRegistry circuitBreakerRegistry,
        RetryRegistry retryRegistry
    ) {
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.retryRegistry = retryRegistry;
        
        // Map providers by name (without resilience wrapper)
        this.providers = providerList.stream()
            .filter(p -> !(p instanceof ResilientCloudStorageProvider))
            .collect(Collectors.toMap(
                CloudStorageProvider::getProviderName,
                Function.identity()
            ));

        log.info("Initialized CloudStorageProviderFactory with {} providers: {}",
            providers.size(), providers.keySet());
    }

    /**
     * Get provider by name
     */
    public CloudStorageProvider getProvider(String providerName) {
        CloudStorageProvider provider = providers.get(providerName.toUpperCase());
        if (provider == null) {
            throw new IllegalArgumentException("Unknown storage provider: " + providerName);
        }
        return provider;
    }

    /**
     * Get provider with resilience patterns
     */
    public CloudStorageProvider getResilientProvider(String providerName) {
        CloudStorageProvider baseProvider = getProvider(providerName);
        
        // Get or create circuit breaker for this provider
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(
            "storage-" + providerName.toLowerCase()
        );
        
        // Get or create retry for this provider
        Retry retry = retryRegistry.retry("storage-" + providerName.toLowerCase());
        
        return new ResilientCloudStorageProvider(baseProvider, circuitBreaker, retry);
    }

    /**
     * Get default provider (first available)
     */
    public CloudStorageProvider getDefaultProvider() {
        return providers.values().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No storage providers available"));
    }

    /**
     * Check if provider is available
     */
    public boolean isProviderAvailable(String providerName) {
        return providers.containsKey(providerName.toUpperCase());
    }

    /**
     * Get all available provider names
     */
    public List<String> getAvailableProviders() {
        return List.copyOf(providers.keySet());
    }
}

