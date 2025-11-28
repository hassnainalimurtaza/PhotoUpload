package com.photoupload.service.repository;

import com.photoupload.common.domain.Photo;
import com.photoupload.common.domain.PhotoStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Photo entity.
 * Implements Repository pattern for data access.
 */
@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {

    /**
     * Find photos by user ID with pagination
     */
    Page<Photo> findByUserId(String userId, Pageable pageable);

    /**
     * Find photos by status
     */
    Page<Photo> findByStatus(PhotoStatus status, Pageable pageable);

    /**
     * Find photos by user ID and status
     */
    Page<Photo> findByUserIdAndStatus(String userId, PhotoStatus status, Pageable pageable);

    /**
     * Find photo by checksum (for deduplication)
     */
    Optional<Photo> findByChecksum(String checksum);

    /**
     * Find photos that failed and need retry
     */
    @Query("SELECT p FROM Photo p WHERE p.status = :status AND p.retryCount < :maxRetries")
    List<Photo> findFailedPhotosForRetry(@Param("status") PhotoStatus status, @Param("maxRetries") int maxRetries);

    /**
     * Find photo with pessimistic lock for concurrent updates
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Photo p WHERE p.id = :id")
    Optional<Photo> findByIdForUpdate(@Param("id") Long id);

    /**
     * Count photos by status for monitoring
     */
    long countByStatus(PhotoStatus status);

    /**
     * Count photos by user ID
     */
    long countByUserId(String userId);
}

