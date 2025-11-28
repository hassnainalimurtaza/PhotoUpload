package com.photoupload.storage.impl;

import com.azure.storage.blob.*;
import com.azure.storage.blob.models.*;
import com.azure.storage.blob.sas.*;
import com.photoupload.common.exception.StorageException;
import com.photoupload.storage.CloudStorageProvider;
import com.photoupload.storage.StorageMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Azure Blob Storage implementation of CloudStorageProvider.
 * Implements Strategy pattern for Azure-specific operations.
 * Adapter pattern: Adapts Azure BlobServiceClient to CloudStorageProvider interface.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "azure")
public class AzureStorageProvider implements CloudStorageProvider {

    private final BlobContainerClient containerClient;
    private final String containerName;

    public AzureStorageProvider(
        BlobServiceClient blobServiceClient,
        @Value("${azure.storage.container-name}") String containerName
    ) {
        this.containerClient = blobServiceClient.getBlobContainerClient(containerName);
        this.containerName = containerName;
        
        // Ensure container exists
        if (!containerClient.exists()) {
            containerClient.create();
        }
        
        log.info("Initialized AzureStorageProvider with container: {}", containerName);
    }

    @Override
    public String upload(String key, InputStream inputStream, String contentType, long fileSize) {
        try {
            log.debug("Uploading file to Azure: key={}, contentType={}, size={}", key, contentType, fileSize);

            BlobClient blobClient = containerClient.getBlobClient(key);
            
            BlobHttpHeaders headers = new BlobHttpHeaders()
                .setContentType(contentType);

            blobClient.upload(inputStream, fileSize, true);
            blobClient.setHttpHeaders(headers);

            String url = blobClient.getBlobUrl();
            log.info("Successfully uploaded file to Azure: key={}", key);

            return url;
        } catch (Exception e) {
            log.error("Failed to upload file to Azure: key={}", key, e);
            throw new StorageException("Azure", "upload", e.getMessage(), e);
        }
    }

    @Override
    public InputStream download(String key) {
        try {
            log.debug("Downloading file from Azure: key={}", key);

            BlobClient blobClient = containerClient.getBlobClient(key);
            
            if (!blobClient.exists()) {
                throw new StorageException("Azure", "download", "File not found: " + key, null);
            }

            return blobClient.openInputStream();
        } catch (Exception e) {
            log.error("Failed to download file from Azure: key={}", key, e);
            throw new StorageException("Azure", "download", e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            log.debug("Deleting file from Azure: key={}", key);

            BlobClient blobClient = containerClient.getBlobClient(key);
            boolean deleted = blobClient.deleteIfExists();

            if (deleted) {
                log.info("Successfully deleted file from Azure: key={}", key);
            } else {
                log.warn("File not found for deletion in Azure: key={}", key);
            }

            return deleted;
        } catch (Exception e) {
            log.error("Failed to delete file from Azure: key={}", key, e);
            throw new StorageException("Azure", "delete", e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            BlobClient blobClient = containerClient.getBlobClient(key);
            return blobClient.exists();
        } catch (Exception e) {
            log.error("Error checking if file exists in Azure: key={}", key, e);
            throw new StorageException("Azure", "exists", e.getMessage(), e);
        }
    }

    @Override
    public String generatePresignedUrl(String key, Duration duration) {
        try {
            log.debug("Generating SAS URL for Azure blob: key={}, duration={}", key, duration);

            BlobClient blobClient = containerClient.getBlobClient(key);

            OffsetDateTime expiryTime = OffsetDateTime.now().plus(duration);
            
            BlobSasPermission permission = new BlobSasPermission().setReadPermission(true);
            BlobServiceSasSignatureValues sasValues = new BlobServiceSasSignatureValues(expiryTime, permission);

            String sasToken = blobClient.generateSas(sasValues);
            String url = blobClient.getBlobUrl() + "?" + sasToken;

            log.debug("Generated SAS URL: key={}", key);
            return url;
        } catch (Exception e) {
            log.error("Failed to generate SAS URL: key={}", key, e);
            throw new StorageException("Azure", "generatePresignedUrl", e.getMessage(), e);
        }
    }

    @Override
    public StorageMetadata getMetadata(String key) {
        try {
            BlobClient blobClient = containerClient.getBlobClient(key);

            if (!blobClient.exists()) {
                throw new StorageException("Azure", "getMetadata", "File not found: " + key, null);
            }

            BlobProperties properties = blobClient.getProperties();

            Map<String, String> userMetadata = new HashMap<>();
            if (properties.getMetadata() != null) {
                userMetadata.putAll(properties.getMetadata());
            }

            return StorageMetadata.builder()
                .key(key)
                .contentType(properties.getContentType())
                .contentLength(properties.getBlobSize())
                .etag(properties.getETag())
                .lastModified(properties.getLastModified().toInstant())
                .userMetadata(userMetadata)
                .build();
        } catch (Exception e) {
            log.error("Failed to get metadata from Azure: key={}", key, e);
            throw new StorageException("Azure", "getMetadata", e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "Azure";
    }
}

