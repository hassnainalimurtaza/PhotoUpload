package com.photoupload.storage.impl;

import com.photoupload.common.exception.StorageException;
import com.photoupload.storage.CloudStorageProvider;
import com.photoupload.storage.StorageMetadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;

/**
 * Local file system implementation of CloudStorageProvider for development.
 * Stores files locally instead of cloud storage.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "local")
public class LocalFileStorageProvider implements CloudStorageProvider {

    private final Path basePath;

    public LocalFileStorageProvider(
        @Value("${storage.local.base-path:./uploads}") String basePath
    ) {
        this.basePath = Paths.get(basePath).toAbsolutePath();
        try {
            Files.createDirectories(this.basePath);
            log.info("Initialized LocalFileStorageProvider with base path: {}", this.basePath);
        } catch (IOException e) {
            throw new StorageException("Failed to create storage directory: " + basePath, e);
        }
    }

    @Override
    public String upload(String key, InputStream inputStream, String contentType, long fileSize) {
        try {
            log.debug("Uploading file to local storage: key={}, size={}", key, fileSize);

            Path filePath = basePath.resolve(key);
            Files.createDirectories(filePath.getParent());
            
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);

            String url = "file://" + filePath.toString();
            log.info("Successfully uploaded file to local storage: key={}, path={}", key, filePath);

            return url;
        } catch (IOException e) {
            log.error("Failed to upload file to local storage: key={}", key, e);
            throw new StorageException("Local", "upload", e.getMessage(), e);
        }
    }

    @Override
    public InputStream download(String key) {
        try {
            log.debug("Downloading file from local storage: key={}", key);

            Path filePath = basePath.resolve(key);
            if (!Files.exists(filePath)) {
                throw new StorageException("Local", "download", "File not found: " + key, null);
            }

            return new FileInputStream(filePath.toFile());
        } catch (IOException e) {
            log.error("Failed to download file from local storage: key={}", key, e);
            throw new StorageException("Local", "download", e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            log.debug("Deleting file from local storage: key={}", key);

            Path filePath = basePath.resolve(key);
            boolean deleted = Files.deleteIfExists(filePath);

            if (deleted) {
                log.info("Successfully deleted file from local storage: key={}", key);
            } else {
                log.warn("File not found for deletion in local storage: key={}", key);
            }

            return deleted;
        } catch (IOException e) {
            log.error("Failed to delete file from local storage: key={}", key, e);
            throw new StorageException("Local", "delete", e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String key) {
        Path filePath = basePath.resolve(key);
        return Files.exists(filePath);
    }

    @Override
    public String generatePresignedUrl(String key, Duration duration) {
        // For local storage, just return the file URL
        Path filePath = basePath.resolve(key);
        return "file://" + filePath.toString();
    }

    @Override
    public StorageMetadata getMetadata(String key) {
        try {
            Path filePath = basePath.resolve(key);
            if (!Files.exists(filePath)) {
                throw new StorageException("Local", "getMetadata", "File not found: " + key, null);
            }

            return StorageMetadata.builder()
                .key(key)
                .contentLength(Files.size(filePath))
                .lastModified(Files.getLastModifiedTime(filePath).toInstant())
                .userMetadata(new HashMap<>())
                .build();
        } catch (IOException e) {
            log.error("Failed to get metadata from local storage: key={}", key, e);
            throw new StorageException("Local", "getMetadata", e.getMessage(), e);
        }
    }

    @Override
    public String getProviderName() {
        return "Local";
    }
}

