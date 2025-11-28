package com.photoupload.storage;

import java.io.InputStream;
import java.time.Duration;

/**
 * Interface Segregation Principle: Focused interface for cloud storage operations.
 * Strategy Pattern: Multiple implementations for different cloud providers.
 * Open/Closed Principle: Extensible for new providers without modifying existing code.
 */
public interface CloudStorageProvider {

    /**
     * Upload file to cloud storage
     *
     * @param key         Unique storage key
     * @param inputStream File content stream
     * @param contentType MIME type
     * @param fileSize    Size in bytes
     * @return Public URL of uploaded file
     */
    String upload(String key, InputStream inputStream, String contentType, long fileSize);

    /**
     * Download file from cloud storage
     *
     * @param key Storage key
     * @return File content as InputStream
     */
    InputStream download(String key);

    /**
     * Delete file from cloud storage
     *
     * @param key Storage key
     * @return true if deleted successfully
     */
    boolean delete(String key);

    /**
     * Check if file exists
     *
     * @param key Storage key
     * @return true if exists
     */
    boolean exists(String key);

    /**
     * Generate pre-signed URL for temporary access
     *
     * @param key      Storage key
     * @param duration Validity duration
     * @return Pre-signed URL
     */
    String generatePresignedUrl(String key, Duration duration);

    /**
     * Get metadata about stored file
     *
     * @param key Storage key
     * @return File metadata
     */
    StorageMetadata getMetadata(String key);

    /**
     * Get provider name
     *
     * @return Provider identifier (S3, GCS, Azure)
     */
    String getProviderName();
}

