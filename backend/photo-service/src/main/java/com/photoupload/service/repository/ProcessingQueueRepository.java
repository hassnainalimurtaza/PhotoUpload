package com.photoupload.service.repository;

import com.photoupload.common.domain.ProcessingQueue;
import com.photoupload.common.domain.ProcessingQueue.CommandType;
import com.photoupload.common.domain.ProcessingQueue.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for ProcessingQueue entity.
 * Supports database-backed queue for fallback scenarios.
 */
@Repository
public interface ProcessingQueueRepository extends JpaRepository<ProcessingQueue, Long> {

    /**
     * Find items ready for processing
     */
    @Query("SELECT q FROM ProcessingQueue q WHERE q.status = :status AND " +
           "(q.nextRetryAt IS NULL OR q.nextRetryAt <= :now) " +
           "ORDER BY q.createdAt ASC")
    List<ProcessingQueue> findReadyForProcessing(
        @Param("status") QueueStatus status,
        @Param("now") Instant now
    );

    /**
     * Find items by photo ID
     */
    List<ProcessingQueue> findByPhotoId(Long photoId);

    /**
     * Find items by status
     */
    List<ProcessingQueue> findByStatus(QueueStatus status);

    /**
     * Find dead letter queue items for alerting
     */
    List<ProcessingQueue> findByStatus(QueueStatus status, org.springframework.data.domain.Pageable pageable);

    /**
     * Find items that exceeded max retries
     */
    @Query("SELECT q FROM ProcessingQueue q WHERE q.retryCount >= q.maxRetries AND q.status != :dlqStatus")
    List<ProcessingQueue> findItemsExceedingMaxRetries(@Param("dlqStatus") QueueStatus dlqStatus);

    /**
     * Count items by status for monitoring
     */
    long countByStatus(QueueStatus status);

    /**
     * Delete completed items older than specified time
     */
    void deleteByStatusAndCompletedAtBefore(QueueStatus status, Instant before);
}

