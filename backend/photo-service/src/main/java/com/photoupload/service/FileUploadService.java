package com.photoupload.service;

import com.photoupload.common.domain.Photo;
import com.photoupload.common.domain.PhotoEvent;
import com.photoupload.common.domain.PhotoStatus;
import com.photoupload.common.dto.PhotoUploadRequest;
import com.photoupload.common.event.PhotoUploadedEvent;
import com.photoupload.common.exception.StorageException;
import com.photoupload.common.util.CorrelationIdGenerator;
import com.photoupload.eventbus.EventPublisher;
import com.photoupload.service.repository.PhotoEventRepository;
import com.photoupload.service.repository.PhotoRepository;
import com.photoupload.storage.CloudStorageProvider;
import com.photoupload.storage.factory.CloudStorageProviderFactory;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for handling file uploads with retry mechanism.
 * Single Responsibility: Handles only file upload logic.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileUploadService {

    private final PhotoRepository photoRepository;
    private final PhotoEventRepository photoEventRepository;
    private final CloudStorageProviderFactory storageProviderFactory;
    private final EventPublisher eventPublisher;

    @Value("${storage.provider:s3}")
    private String defaultStorageProvider;

    /**
     * Upload file to cloud storage with retry
     * Retry annotation provides automatic retry with exponential backoff
     */
    @Retry(name = "fileUpload", fallbackMethod = "uploadFallback")
    @Transactional
    public Photo uploadFile(PhotoUploadRequest request) {
        String correlationId = CorrelationIdGenerator.generate();
        CorrelationIdGenerator.set(correlationId);

        try {
            log.info("Starting file upload: filename={}, userId={}, correlationId={}",
                request.getOriginalFilename(), request.getUserId(), correlationId);

            // Validate request
            request.validate();

            // Calculate checksum for deduplication
            String checksum = calculateChecksum(request.getFile().getInputStream());

            // Check for duplicate
            Optional<Photo> existingPhoto = photoRepository.findByChecksum(checksum);
            if (existingPhoto.isPresent()) {
                log.warn("Duplicate photo detected: checksum={}, existingId={}", 
                    checksum, existingPhoto.get().getId());
                throw new IllegalStateException("Photo already exists with ID: " + existingPhoto.get().getId());
            }

            // Create photo entity (Builder pattern)
            Photo photo = Photo.builder()
                .userId(request.getUserId())
                .originalFileName(request.getOriginalFilename())
                .contentType(request.getContentType())
                .fileSize(request.getFileSize())
                .status(PhotoStatus.PENDING)
                .checksum(checksum)
                .build();

            // Save to database first
            photo = photoRepository.save(photo);
            log.debug("Photo entity created: id={}", photo.getId());

            // Record event
            recordEvent(photo.getId(), PhotoEvent.EventType.PHOTO_UPLOAD_STARTED, 
                "File upload started", correlationId, true);

            // Generate storage key
            String storageKey = generateStorageKey(request.getUserId(), photo.getId(), 
                request.getOriginalFilename());

            // Get storage provider with resilience
            CloudStorageProvider storageProvider = storageProviderFactory
                .getResilientProvider(defaultStorageProvider);

            // Update status to uploading
            photo.transitionTo(PhotoStatus.UPLOADING);
            photoRepository.save(photo);

            // Upload to cloud storage
            String storageUrl = storageProvider.upload(
                storageKey,
                request.getFile().getInputStream(),
                request.getContentType(),
                request.getFileSize()
            );

            // Update photo with storage information
            photo.setStorageKey(storageKey);
            photo.setStorageUrl(storageUrl);
            photo.transitionTo(PhotoStatus.UPLOADED);
            photo = photoRepository.save(photo);

            log.info("File uploaded successfully: photoId={}, storageKey={}", photo.getId(), storageKey);

            // Record success event
            recordEvent(photo.getId(), PhotoEvent.EventType.PHOTO_UPLOADED,
                "File uploaded to storage", correlationId, true);

            // Publish event
            PhotoUploadedEvent event = PhotoUploadedEvent.of(
                photo.getId(),
                photo.getUserId(),
                storageKey,
                photo.getOriginalFileName(),
                photo.getContentType(),
                photo.getFileSize(),
                correlationId
            );
            eventPublisher.publish("PhotoUploadedEvent", event);

            return photo;

        } catch (Exception e) {
            log.error("File upload failed: filename={}, error={}", 
                request.getOriginalFilename(), e.getMessage(), e);
            throw new StorageException("Failed to upload file: " + e.getMessage(), e);
        } finally {
            CorrelationIdGenerator.clear();
        }
    }

    /**
     * Fallback method when upload fails after all retries
     */
    @Transactional
    public Photo uploadFallback(PhotoUploadRequest request, Exception e) {
        String correlationId = CorrelationIdGenerator.getOrGenerate();
        
        log.error("File upload failed after all retries: filename={}, error={}",
            request.getOriginalFilename(), e.getMessage());

        // Create photo entity in FAILED state
        Photo photo = Photo.builder()
            .userId(request.getUserId())
            .originalFileName(request.getOriginalFilename())
            .contentType(request.getContentType())
            .fileSize(request.getFileSize())
            .status(PhotoStatus.FAILED)
            .lastError(e.getMessage())
            .retryCount(3)
            .build();

        photo = photoRepository.save(photo);

        // Record failure event
        recordEvent(photo.getId(), PhotoEvent.EventType.PHOTO_PROCESSING_FAILED,
            "Upload failed: " + e.getMessage(), correlationId, false);

        return photo;
    }

    /**
     * Generate unique storage key
     */
    private String generateStorageKey(String userId, Long photoId, String originalFilename) {
        String extension = "";
        int lastDot = originalFilename.lastIndexOf('.');
        if (lastDot > 0) {
            extension = originalFilename.substring(lastDot);
        }

        return String.format("photos/%s/%d/%s%s",
            userId,
            photoId,
            UUID.randomUUID().toString(),
            extension
        );
    }

    /**
     * Calculate SHA-256 checksum for deduplication
     */
    private String calculateChecksum(InputStream inputStream) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buffer = new byte[8192];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }

            byte[] hashBytes = digest.digest();
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (Exception e) {
            log.error("Failed to calculate checksum", e);
            return UUID.randomUUID().toString(); // Fallback to UUID
        }
    }

    /**
     * Record event in database
     */
    private void recordEvent(Long photoId, PhotoEvent.EventType eventType, 
                             String details, String correlationId, boolean success) {
        PhotoEvent event = PhotoEvent.builder()
            .photoId(photoId)
            .eventType(eventType)
            .details(details)
            .correlationId(correlationId)
            .success(success)
            .build();

        photoEventRepository.save(event);
    }
}

