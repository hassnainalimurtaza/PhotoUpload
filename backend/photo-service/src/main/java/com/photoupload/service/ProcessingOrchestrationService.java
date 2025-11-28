package com.photoupload.service;

import com.photoupload.common.domain.Photo;
import com.photoupload.common.domain.PhotoEvent;
import com.photoupload.common.domain.PhotoStatus;
import com.photoupload.common.event.*;
import com.photoupload.common.exception.PhotoProcessingException;
import com.photoupload.common.util.CorrelationIdGenerator;
import com.photoupload.eventbus.EventPublisher;
import com.photoupload.service.repository.PhotoEventRepository;
import com.photoupload.service.repository.PhotoRepository;
import com.photoupload.storage.CloudStorageProvider;
import com.photoupload.storage.factory.CloudStorageProviderFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

/**
 * Saga Pattern: Orchestrates distributed photo processing workflow.
 * Command Pattern: Executes processing commands asynchronously.
 * Observer Pattern: Publishes events for each processing stage.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessingOrchestrationService {

    private final PhotoRepository photoRepository;
    private final PhotoEventRepository photoEventRepository;
    private final MetadataExtractionService metadataService;
    private final ThumbnailGenerationService thumbnailService;
    private final EventPublisher eventPublisher;
    private final CloudStorageProviderFactory storageProviderFactory;

    @Value("${storage.provider:s3}")
    private String defaultStorageProvider;

    @Value("${processing.max-retries:3}")
    private int maxRetries;

    /**
     * Start photo processing workflow (Saga orchestration)
     */
    @Async("photoProcessingExecutor")
    @Transactional
    public CompletableFuture<Void> startProcessing(Photo photo) {
        String correlationId = CorrelationIdGenerator.generate();
        CorrelationIdGenerator.set(correlationId);

        try {
            log.info("Starting photo processing saga: photoId={}, correlationId={}",
                photo.getId(), correlationId);

            // Update status
            photo.transitionTo(PhotoStatus.PROCESSING);
            photoRepository.save(photo);

            // Publish processing started event
            PhotoProcessingStartedEvent startedEvent = PhotoProcessingStartedEvent.of(
                photo.getId(), photo.getUserId(), correlationId
            );
            eventPublisher.publishWithCorrelation("PhotoProcessingStartedEvent", startedEvent, correlationId);

            recordEvent(photo.getId(), PhotoEvent.EventType.PHOTO_PROCESSING_STARTED,
                "Processing started", correlationId, true);

            // Execute processing steps in parallel
            CompletableFuture<String> thumbnailFuture = generateThumbnail(photo, correlationId);
            CompletableFuture<String> metadataFuture = extractMetadata(photo, correlationId);

            // Wait for both to complete
            CompletableFuture.allOf(thumbnailFuture, metadataFuture)
                .thenAccept(v -> {
                    try {
                        String thumbnailUrl = thumbnailFuture.join();
                        String metadata = metadataFuture.join();

                        completeProcessing(photo, thumbnailUrl, metadata, correlationId);
                    } catch (Exception e) {
                        failProcessing(photo, "Completion failed", e, correlationId);
                    }
                })
                .exceptionally(ex -> {
                    failProcessing(photo, "Processing failed", ex, correlationId);
                    return null;
                });

            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Failed to start processing saga: photoId={}", photo.getId(), e);
            failProcessing(photo, "Failed to start processing", e, correlationId);
            throw new PhotoProcessingException(photo.getId(), "start", e.getMessage(), e);
        } finally {
            CorrelationIdGenerator.clear();
        }
    }

    /**
     * Generate thumbnail
     */
    private CompletableFuture<String> generateThumbnail(Photo photo, String correlationId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Generating thumbnail: photoId={}", photo.getId());

                CloudStorageProvider storage = storageProviderFactory.getResilientProvider(defaultStorageProvider);
                String thumbnailUrl = thumbnailService.generateThumbnail(photo, storage);

                recordEvent(photo.getId(), PhotoEvent.EventType.PHOTO_THUMBNAIL_GENERATED,
                    "Thumbnail generated", correlationId, true);

                return thumbnailUrl;
            } catch (Exception e) {
                log.error("Thumbnail generation failed: photoId={}", photo.getId(), e);
                recordEvent(photo.getId(), PhotoEvent.EventType.PHOTO_THUMBNAIL_GENERATED,
                    "Thumbnail generation failed: " + e.getMessage(), correlationId, false);
                throw new PhotoProcessingException(photo.getId(), "thumbnail", e.getMessage(), e);
            }
        });
    }

    /**
     * Extract metadata
     */
    private CompletableFuture<String> extractMetadata(Photo photo, String correlationId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                log.debug("Extracting metadata: photoId={}", photo.getId());

                CloudStorageProvider storage = storageProviderFactory.getResilientProvider(defaultStorageProvider);
                String metadata = metadataService.extractMetadata(photo, storage);

                recordEvent(photo.getId(), PhotoEvent.EventType.PHOTO_METADATA_EXTRACTED,
                    "Metadata extracted", correlationId, true);

                return metadata;
            } catch (Exception e) {
                log.warn("Metadata extraction failed (non-critical): photoId={}", photo.getId(), e);
                recordEvent(photo.getId(), PhotoEvent.EventType.PHOTO_METADATA_EXTRACTED,
                    "Metadata extraction failed: " + e.getMessage(), correlationId, false);
                return "{}"; // Return empty JSON on failure
            }
        });
    }

    /**
     * Complete processing successfully
     */
    @Transactional
    public void completeProcessing(Photo photo, String thumbnailUrl, String metadata, String correlationId) {
        try {
            log.info("Completing photo processing: photoId={}", photo.getId());

            // Reload photo for update
            Photo updatedPhoto = photoRepository.findById(photo.getId())
                .orElseThrow(() -> new PhotoProcessingException(photo.getId(), "complete", "Photo not found"));

            // Update photo
            updatedPhoto.setThumbnailUrl(thumbnailUrl);
            updatedPhoto.setMetadata(metadata);
            updatedPhoto.transitionTo(PhotoStatus.COMPLETED);
            photoRepository.save(updatedPhoto);

            // Record event
            recordEvent(photo.getId(), PhotoEvent.EventType.PHOTO_PROCESSING_COMPLETED,
                "Processing completed successfully", correlationId, true);

            // Publish completion event
            PhotoProcessingCompletedEvent completedEvent = PhotoProcessingCompletedEvent.of(
                photo.getId(),
                photo.getUserId(),
                thumbnailUrl,
                photo.getWidth(),
                photo.getHeight(),
                metadata,
                correlationId
            );
            eventPublisher.publishWithCorrelation("PhotoProcessingCompletedEvent", completedEvent, correlationId);

            log.info("Photo processing completed successfully: photoId={}", photo.getId());

        } catch (Exception e) {
            log.error("Failed to complete processing: photoId={}", photo.getId(), e);
            failProcessing(photo, "Completion failed", e, correlationId);
        }
    }

    /**
     * Handle processing failure
     */
    @Transactional
    public void failProcessing(Photo photo, String stage, Throwable error, String correlationId) {
        try {
            log.error("Photo processing failed: photoId={}, stage={}, error={}",
                photo.getId(), stage, error.getMessage());

            // Reload photo for update
            Photo updatedPhoto = photoRepository.findById(photo.getId())
                .orElse(photo);

            updatedPhoto.incrementRetryCount();
            updatedPhoto.setLastError(error.getMessage());

            boolean willRetry = updatedPhoto.shouldRetry(maxRetries);

            if (willRetry) {
                updatedPhoto.transitionTo(PhotoStatus.RETRYING);
                log.info("Scheduling retry for photo: photoId={}, retryCount={}",
                    photo.getId(), updatedPhoto.getRetryCount());
            } else {
                updatedPhoto.transitionTo(PhotoStatus.FAILED);
                log.error("Max retries exceeded for photo: photoId={}", photo.getId());
            }

            photoRepository.save(updatedPhoto);

            // Record event
            recordEvent(photo.getId(), PhotoEvent.EventType.PHOTO_PROCESSING_FAILED,
                String.format("Processing failed at %s: %s", stage, error.getMessage()),
                correlationId, false);

            // Publish failure event
            PhotoProcessingFailedEvent failedEvent = PhotoProcessingFailedEvent.of(
                photo.getId(),
                photo.getUserId(),
                error.getMessage(),
                error.getClass().getSimpleName(),
                updatedPhoto.getRetryCount(),
                willRetry,
                correlationId
            );
            eventPublisher.publishWithCorrelation("PhotoProcessingFailedEvent", failedEvent, correlationId);

            // Schedule retry if applicable
            if (willRetry) {
                scheduleRetry(updatedPhoto, correlationId);
            }

        } catch (Exception e) {
            log.error("Failed to handle processing failure: photoId={}", photo.getId(), e);
        }
    }

    /**
     * Schedule retry for failed processing
     */
    @Async("photoProcessingExecutor")
    public void scheduleRetry(Photo photo, String correlationId) {
        try {
            // Exponential backoff: 1s, 2s, 4s
            long backoffSeconds = (long) Math.pow(2, photo.getRetryCount() - 1);
            Thread.sleep(backoffSeconds * 1000);

            log.info("Retrying photo processing: photoId={}, attempt={}",
                photo.getId(), photo.getRetryCount());

            recordEvent(photo.getId(), PhotoEvent.EventType.PHOTO_RETRY_SCHEDULED,
                "Retry scheduled after " + backoffSeconds + "s", correlationId, true);

            startProcessing(photo);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Retry scheduling interrupted: photoId={}", photo.getId());
        } catch (Exception e) {
            log.error("Failed to schedule retry: photoId={}", photo.getId(), e);
        }
    }

    /**
     * Retry processing for failed photo
     */
    @Transactional
    public void retryProcessing(Photo photo) {
        String correlationId = CorrelationIdGenerator.generate();
        
        log.info("Manual retry triggered: photoId={}, correlationId={}", photo.getId(), correlationId);

        photo.transitionTo(PhotoStatus.PENDING);
        photo.setRetryCount(0);
        photo.setLastError(null);
        photoRepository.save(photo);

        startProcessing(photo);
    }

    /**
     * Delete photo and cleanup storage
     */
    @Async("photoProcessingExecutor")
    @Transactional
    public void deletePhoto(Photo photo) {
        String correlationId = CorrelationIdGenerator.generate();

        try {
            log.info("Deleting photo storage: photoId={}, storageKey={}",
                photo.getId(), photo.getStorageKey());

            // Delete from cloud storage
            CloudStorageProvider storage = storageProviderFactory.getResilientProvider(defaultStorageProvider);
            
            if (photo.getStorageKey() != null) {
                storage.delete(photo.getStorageKey());
            }

            // Publish deletion event
            PhotoDeletedEvent deletedEvent = PhotoDeletedEvent.of(
                photo.getId(),
                photo.getUserId(),
                photo.getStorageKey(),
                correlationId
            );
            eventPublisher.publishWithCorrelation("PhotoDeletedEvent", deletedEvent, correlationId);

            recordEvent(photo.getId(), PhotoEvent.EventType.PHOTO_DELETED,
                "Photo deleted", correlationId, true);

            log.info("Photo storage deleted successfully: photoId={}", photo.getId());

        } catch (Exception e) {
            log.error("Failed to delete photo storage: photoId={}", photo.getId(), e);
            // Continue with database deletion even if storage deletion fails
        }
    }

    /**
     * Record event in database
     */
    private void recordEvent(Long photoId, PhotoEvent.EventType eventType,
                             String details, String correlationId, boolean success) {
        try {
            PhotoEvent event = PhotoEvent.builder()
                .photoId(photoId)
                .eventType(eventType)
                .details(details)
                .correlationId(correlationId)
                .success(success)
                .build();

            photoEventRepository.save(event);
        } catch (Exception e) {
            log.error("Failed to record event: photoId={}, eventType={}", photoId, eventType, e);
        }
    }
}

