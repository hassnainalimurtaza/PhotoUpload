package com.photoupload.service;

import com.photoupload.common.domain.Photo;
import com.photoupload.common.domain.PhotoEvent;
import com.photoupload.common.domain.PhotoStatus;
import com.photoupload.common.dto.PhotoEventResponse;
import com.photoupload.common.dto.PhotoResponse;
import com.photoupload.common.dto.PhotoUploadRequest;
import com.photoupload.common.exception.PhotoNotFoundException;
import com.photoupload.service.mapper.PhotoMapper;
import com.photoupload.service.repository.PhotoEventRepository;
import com.photoupload.service.repository.PhotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Facade Pattern: Unified API for photo operations.
 * Single Responsibility: Coordinates between lower-level services.
 * Dependency Inversion: Depends on abstractions (repositories, services).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoService {

    private final PhotoRepository photoRepository;
    private final PhotoEventRepository photoEventRepository;
    private final FileUploadService fileUploadService;
    private final ProcessingOrchestrationService orchestrationService;
    private final PhotoMapper photoMapper;

    /**
     * Upload a new photo
     */
    @Transactional
    public PhotoResponse uploadPhoto(PhotoUploadRequest request) {
        log.info("Uploading photo for user: {}", request.getUserId());

        // Delegate to file upload service
        Photo photo = fileUploadService.uploadFile(request);

        // Trigger async processing
        orchestrationService.startProcessing(photo);

        return photoMapper.toResponse(photo);
    }

    /**
     * Get photo by ID with caching
     */
    @Cacheable(value = "photos", key = "#id")
    @Transactional(readOnly = true)
    public PhotoResponse getPhoto(Long id) {
        log.debug("Retrieving photo: {}", id);

        Photo photo = photoRepository.findById(id)
            .orElseThrow(() -> new PhotoNotFoundException(id));

        return photoMapper.toResponse(photo);
    }

    /**
     * Get all photos for user with pagination
     */
    @Transactional(readOnly = true)
    public Page<PhotoResponse> getUserPhotos(String userId, Pageable pageable) {
        log.debug("Retrieving photos for user: {}, page: {}", userId, pageable.getPageNumber());

        Page<Photo> photos = photoRepository.findByUserId(userId, pageable);
        return photos.map(photoMapper::toResponse);
    }

    /**
     * Get photos by status
     */
    @Transactional(readOnly = true)
    public Page<PhotoResponse> getPhotosByStatus(PhotoStatus status, Pageable pageable) {
        log.debug("Retrieving photos with status: {}", status);

        Page<Photo> photos = photoRepository.findByStatus(status, pageable);
        return photos.map(photoMapper::toResponse);
    }

    /**
     * Get photos by user and status
     */
    @Transactional(readOnly = true)
    public Page<PhotoResponse> getUserPhotosByStatus(String userId, PhotoStatus status, Pageable pageable) {
        log.debug("Retrieving photos for user: {}, status: {}", userId, status);

        Page<Photo> photos = photoRepository.findByUserIdAndStatus(userId, status, pageable);
        return photos.map(photoMapper::toResponse);
    }

    /**
     * Get event log for photo
     */
    @Transactional(readOnly = true)
    public List<PhotoEventResponse> getPhotoEvents(Long photoId) {
        log.debug("Retrieving events for photo: {}", photoId);

        // Verify photo exists
        if (!photoRepository.existsById(photoId)) {
            throw new PhotoNotFoundException(photoId);
        }

        List<PhotoEvent> events = photoEventRepository.findByPhotoIdOrderByTimestampAsc(photoId);
        return events.stream()
            .map(photoMapper::toEventResponse)
            .collect(Collectors.toList());
    }

    /**
     * Get paginated event log for photo
     */
    @Transactional(readOnly = true)
    public Page<PhotoEventResponse> getPhotoEvents(Long photoId, Pageable pageable) {
        log.debug("Retrieving paginated events for photo: {}", photoId);

        if (!photoRepository.existsById(photoId)) {
            throw new PhotoNotFoundException(photoId);
        }

        Page<PhotoEvent> events = photoEventRepository.findByPhotoIdOrderByTimestampDesc(photoId, pageable);
        return events.map(photoMapper::toEventResponse);
    }

    /**
     * Delete photo
     */
    @CacheEvict(value = "photos", key = "#id")
    @Transactional
    public void deletePhoto(Long id) {
        log.info("Deleting photo: {}", id);

        Photo photo = photoRepository.findById(id)
            .orElseThrow(() -> new PhotoNotFoundException(id));

        // Trigger deletion orchestration (storage cleanup, event publishing)
        orchestrationService.deletePhoto(photo);

        // Delete from database
        photoRepository.delete(photo);

        log.info("Photo deleted successfully: {}", id);
    }

    /**
     * Retry failed photo processing
     */
    @Transactional
    public void retryProcessing(Long id) {
        log.info("Retrying processing for photo: {}", id);

        Photo photo = photoRepository.findById(id)
            .orElseThrow(() -> new PhotoNotFoundException(id));

        if (photo.getStatus() != PhotoStatus.FAILED) {
            throw new IllegalStateException("Photo is not in FAILED status");
        }

        orchestrationService.retryProcessing(photo);
    }

    /**
     * Get photo count by status for monitoring
     */
    @Transactional(readOnly = true)
    public long countByStatus(PhotoStatus status) {
        return photoRepository.countByStatus(status);
    }

    /**
     * Get user's photo count
     */
    @Transactional(readOnly = true)
    public long countUserPhotos(String userId) {
        return photoRepository.countByUserId(userId);
    }
}

