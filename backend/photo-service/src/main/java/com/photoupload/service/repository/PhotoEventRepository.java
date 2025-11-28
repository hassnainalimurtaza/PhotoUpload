package com.photoupload.service.repository;

import com.photoupload.common.domain.PhotoEvent;
import com.photoupload.common.domain.PhotoEvent.EventType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repository for PhotoEvent entity.
 * Supports event sourcing and workflow tracking.
 */
@Repository
public interface PhotoEventRepository extends JpaRepository<PhotoEvent, Long> {

    /**
     * Find all events for a photo with pagination
     */
    Page<PhotoEvent> findByPhotoIdOrderByTimestampDesc(Long photoId, Pageable pageable);

    /**
     * Find all events for a photo
     */
    List<PhotoEvent> findByPhotoIdOrderByTimestampAsc(Long photoId);

    /**
     * Find events by type
     */
    Page<PhotoEvent> findByEventType(EventType eventType, Pageable pageable);

    /**
     * Find events by correlation ID
     */
    List<PhotoEvent> findByCorrelationIdOrderByTimestampAsc(String correlationId);

    /**
     * Find events in date range
     */
    @Query("SELECT e FROM PhotoEvent e WHERE e.timestamp >= :from AND e.timestamp <= :to ORDER BY e.timestamp DESC")
    List<PhotoEvent> findByTimestampBetween(@Param("from") Instant from, @Param("to") Instant to);

    /**
     * Find failed events for alerting
     */
    @Query("SELECT e FROM PhotoEvent e WHERE e.success = false AND e.timestamp >= :since")
    List<PhotoEvent> findFailedEventsSince(@Param("since") Instant since);

    /**
     * Count events by type for analytics
     */
    long countByEventType(EventType eventType);
}

