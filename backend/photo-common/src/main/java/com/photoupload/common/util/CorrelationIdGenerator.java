package com.photoupload.common.util;

import java.util.UUID;

/**
 * Utility class for generating correlation IDs for request tracing
 */
public class CorrelationIdGenerator {

    private static final ThreadLocal<String> CORRELATION_ID = new ThreadLocal<>();

    private CorrelationIdGenerator() {
        // Utility class
    }

    /**
     * Generate a new correlation ID
     */
    public static String generate() {
        return UUID.randomUUID().toString();
    }

    /**
     * Get current correlation ID or generate new one
     */
    public static String getOrGenerate() {
        String correlationId = CORRELATION_ID.get();
        if (correlationId == null) {
            correlationId = generate();
            CORRELATION_ID.set(correlationId);
        }
        return correlationId;
    }

    /**
     * Set correlation ID for current thread
     */
    public static void set(String correlationId) {
        CORRELATION_ID.set(correlationId);
    }

    /**
     * Get current correlation ID
     */
    public static String get() {
        return CORRELATION_ID.get();
    }

    /**
     * Clear correlation ID from current thread
     */
    public static void clear() {
        CORRELATION_ID.remove();
    }
}

