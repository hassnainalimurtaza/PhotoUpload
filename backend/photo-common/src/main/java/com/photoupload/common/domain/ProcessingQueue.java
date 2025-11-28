package com.photoupload.common.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

/**
 * Database-backed processing queue for fallback when message queue is unavailable.
 * Implements Command pattern for queue-based command execution.
 */
@Entity
@Table(name = "processing_queue", indexes = {
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_next_retry_at", columnList = "next_retry_at"),
    @Index(name = "idx_photo_id", columnList = "photo_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessingQueue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "photo_id", nullable = false)
    private Long photoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "command_type", nullable = false)
    private CommandType commandType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private QueueStatus status = QueueStatus.PENDING;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "max_retries", nullable = false)
    @Builder.Default
    private Integer maxRetries = 3;

    @Column(name = "next_retry_at")
    private Instant nextRetryAt;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private Instant updatedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    /**
     * Command types for processing queue
     */
    public enum CommandType {
        PROCESS_PHOTO,
        GENERATE_THUMBNAIL,
        EXTRACT_METADATA,
        VALIDATE_PHOTO,
        DELETE_PHOTO
    }

    /**
     * Queue item status
     */
    public enum QueueStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED,
        DEAD_LETTER
    }

    /**
     * Calculate next retry time with exponential backoff
     */
    public void scheduleRetry() {
        this.retryCount++;
        if (this.retryCount >= this.maxRetries) {
            this.status = QueueStatus.DEAD_LETTER;
            return;
        }
        // Exponential backoff: 1s, 2s, 4s, 8s...
        long backoffSeconds = (long) Math.pow(2, this.retryCount - 1);
        this.nextRetryAt = Instant.now().plusSeconds(backoffSeconds);
        this.status = QueueStatus.PENDING;
    }

    /**
     * Mark as processing
     */
    public void markProcessing() {
        this.status = QueueStatus.PROCESSING;
    }

    /**
     * Mark as completed
     */
    public void markCompleted() {
        this.status = QueueStatus.COMPLETED;
        this.completedAt = Instant.now();
    }

    /**
     * Mark as failed with error
     */
    public void markFailed(String error) {
        this.status = QueueStatus.FAILED;
        this.lastError = error;
    }

    /**
     * Check if should retry
     */
    public boolean shouldRetry() {
        return this.retryCount < this.maxRetries && this.status == QueueStatus.FAILED;
    }

    /**
     * Check if ready for processing
     */
    public boolean isReadyForProcessing() {
        return this.status == QueueStatus.PENDING &&
               (this.nextRetryAt == null || Instant.now().isAfter(this.nextRetryAt));
    }
}

