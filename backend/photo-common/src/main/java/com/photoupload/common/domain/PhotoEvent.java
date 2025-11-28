package com.photoupload.common.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * Event sourcing entity for photo workflow tracking.
 * Implements Observer pattern for status updates.
 */
@Entity
@Table(name = "photo_events", indexes = {
    @Index(name = "idx_photo_id", columnList = "photo_id"),
    @Index(name = "idx_event_type", columnList = "event_type"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "photo_id", nullable = false)
    private Long photoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "timestamp", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant timestamp;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "source_service", length = 100)
    private String sourceService;

    @Column(name = "success")
    private Boolean success;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * Event types in the photo processing lifecycle
     */
    public enum EventType {
        PHOTO_UPLOAD_STARTED,
        PHOTO_UPLOADED,
        PHOTO_PROCESSING_STARTED,
        PHOTO_VALIDATION_COMPLETED,
        PHOTO_THUMBNAIL_GENERATED,
        PHOTO_METADATA_EXTRACTED,
        PHOTO_PROCESSING_COMPLETED,
        PHOTO_PROCESSING_FAILED,
        PHOTO_RETRY_SCHEDULED,
        PHOTO_DELETED,
        PHOTO_CACHE_INVALIDATED
    }

    /**
     * Factory method for success events
     */
    public static PhotoEvent success(Long photoId, EventType eventType, String details, String correlationId) {
        return PhotoEvent.builder()
            .photoId(photoId)
            .eventType(eventType)
            .details(details)
            .success(true)
            .correlationId(correlationId)
            .build();
    }

    /**
     * Factory method for failure events
     */
    public static PhotoEvent failure(Long photoId, EventType eventType, String errorMessage, String correlationId) {
        return PhotoEvent.builder()
            .photoId(photoId)
            .eventType(eventType)
            .success(false)
            .errorMessage(errorMessage)
            .correlationId(correlationId)
            .build();
    }
}

