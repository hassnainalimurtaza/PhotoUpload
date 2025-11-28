package com.photoupload.storage.impl;

import com.google.cloud.storage.*;
import com.photoupload.common.exception.StorageException;
import com.photoupload.storage.CloudStorageProvider;
import com.photoupload.storage.StorageMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Google Cloud Storage implementation of CloudStorageProvider.
 * Implements Strategy pattern for GCS-specific operations.
 * Adapter pattern: Adapts GCS Storage to CloudStorageProvider interface.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "gcs")
public class GCSStorageProvider implements CloudStorageProvider {

    private final Storage storage;
    private final String bucketName;

    public GCSStorageProvider(
        Storage storage,
        @Value("${gcp.storage.bucket-name}") String bucketName
    ) {
        this.storage = storage;
        this.bucketName = bucketName;
        log.info("Initialized GCSStorageProvider with bucket: {}", bucketName);
    }

    @Override
    public String upload(String key, InputStream inputStream, String contentType, long fileSize) {
        try {
            log.debug("Uploading file to GCS: key={}, contentType={}, size={}", key, contentType, fileSize);

            BlobId blobId = BlobId.of(bucketName, key);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(contentType)
                .build();

            byte[] content = inputStream.readAllBytes();
            Blob blob = storage.create(blobInfo, content);

            String url = String.format("https://storage.googleapis.com/%s/%s", bucketName, key);
            log.info("Successfully uploaded file to GCS: key={}", key);

            return url;
        } catch (IOException e) {
            log.error("Failed to read input stream for GCS upload: key={}", key, e);
            throw new StorageException("GCS", "upload", "Failed to read input stream", e);
        } catch (StorageException e) {
            log.error("Failed to upload file to GCS: key={}", key, e);
            throw new com.photoupload.common.exception.StorageException("GCS", "upload", e.getMessage(), e);
        }
    }

    @Override
    public InputStream download(String key) {
        try {
            log.debug("Downloading file from GCS: key={}", key);

            BlobId blobId = BlobId.of(bucketName, key);
            Blob blob = storage.get(blobId);

            if (blob == null || !blob.exists()) {
                throw new com.photoupload.common.exception.StorageException("GCS", "download", "File not found: " + key, null);
            }

            byte[] content = blob.getContent();
            return new ByteArrayInputStream(content);
        } catch (StorageException e) {
            log.error("Failed to download file from GCS: key={}", key, e);
            throw new com.photoupload.common.exception.StorageException("GCS", "download", e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            log.debug("Deleting file from GCS: key={}", key);

            BlobId blobId = BlobId.of(bucketName, key);
            boolean deleted = storage.delete(blobId);

            if (deleted) {
                log.info("Successfully deleted file from GCS: key={}", key);
            } else {
                log.warn("File not found for deletion in GCS: key={}", key);
            }

            return deleted;
        } catch (StorageException e) {
            log.error("Failed to delete file from GCS: key={}", key, e);
            throw new com.photoupload.common.exception.StorageException("GCS", "delete", e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            BlobId blobId = BlobId.of(bucketName, key);
            Blob blob = storage.get(blobId);
            return blob != null && blob.exists();
        } catch (StorageException e) {
            log.error("Error checking if file exists in GCS: key={}", key, e);
            throw new com.photoupload.common.exception.StorageException("GCS", "exists", e.getMessage(), e);
        }
    }

    @Override
    public String generatePresignedUrl(String key, Duration duration) {
        try {
            log.debug("Generating presigned URL for GCS object: key={}, duration={}", key, duration);

            BlobId blobId = BlobId.of(bucketName, key);
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();

            java.net.URL url = storage.signUrl(
                blobInfo,
                duration.toMinutes(),
                TimeUnit.MINUTES,
                Storage.SignUrlOption.withV4Signature()
            );

            log.debug("Generated presigned URL: key={}", key);
            return url.toString();
        } catch (StorageException e) {
            log.error("Failed to generate presigned URL: key={}", key, e);
            throw new com.photoupload.common.exception.StorageException("GCS", "generatePresignedUrl", e.getMessage(), e);
        }
    }

    @Override
    public com.photoupload.storage.StorageMetadata getMetadata(String key) {
        try {
            BlobId blobId = BlobId.of(bucketName, key);
            Blob blob = storage.get(blobId);

            if (blob == null || !blob.exists()) {
                throw new com.photoupload.common.exception.StorageException("GCS", "getMetadata", "File not found: " + key, null);
            }

            Map<String, String> userMetadata = new HashMap<>();
            if (blob.getMetadata() != null) {
                userMetadata.putAll(blob.getMetadata());
            }

            return com.photoupload.storage.StorageMetadata.builder()
                .key(key)
                .contentType(blob.getContentType())
                .contentLength(blob.getSize())
                .etag(blob.getEtag())
                .lastModified(blob.getUpdateTimeOffsetDateTime().toInstant())
                .userMetadata(userMetadata)
                .storageClass(blob.getStorageClass() != null ? blob.getStorageClass().name() : null)
                .build();
        } catch (StorageException e) {
            log.error("Failed to get metadata from GCS: key={}", key, e);
            throw new com.photoupload.common.exception.StorageException("GCS", "getMetadata", e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "GCS";
    }
}

