package com.photoupload.api.controller;

import com.photoupload.common.domain.PhotoStatus;
import com.photoupload.common.dto.PhotoEventResponse;
import com.photoupload.common.dto.PhotoResponse;
import com.photoupload.common.dto.PhotoUploadRequest;
import com.photoupload.service.PhotoService;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST Controller for photo operations.
 * Implements all API endpoints from specification.
 */
@Slf4j
@RestController
@RequestMapping("/api/photos")
@RequiredArgsConstructor
public class PhotoController {

    private final PhotoService photoService;

    /**
     * POST /api/photos/upload - Upload a new photo
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Timed(value = "photo.upload", description = "Time taken to upload photo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PhotoResponse> uploadPhoto(
        @RequestParam("file") MultipartFile file,
        @RequestParam("userId") String userId,
        @RequestParam(value = "description", required = false) String description,
        @RequestParam(value = "tags", required = false) String tags
    ) {
        log.info("Received photo upload request: userId={}, filename={}, size={}",
            userId, file.getOriginalFilename(), file.getSize());

        PhotoUploadRequest request = PhotoUploadRequest.builder()
            .file(file)
            .userId(userId)
            .description(description)
            .tags(tags)
            .build();

        PhotoResponse response = photoService.uploadPhoto(request);

        log.info("Photo uploaded successfully: photoId={}, status={}",
            response.getId(), response.getStatus());

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }

    /**
     * GET /api/photos/{id} - Get photo by ID
     */
    @GetMapping("/{id}")
    @Timed(value = "photo.get", description = "Time taken to get photo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PhotoResponse> getPhoto(@PathVariable Long id) {
        log.debug("Retrieving photo: id={}", id);

        PhotoResponse response = photoService.getPhoto(id);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/photos - Get all photos with pagination
     * Supports filtering by userId and status
     */
    @GetMapping
    @Timed(value = "photo.list", description = "Time taken to list photos")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PhotoResponse>> getPhotos(
        @RequestParam(value = "userId", required = false) String userId,
        @RequestParam(value = "status", required = false) PhotoStatus status,
        @PageableDefault(size = 20, sort = "uploadedAt") Pageable pageable
    ) {
        log.debug("Retrieving photos: userId={}, status={}, page={}",
            userId, status, pageable.getPageNumber());

        Page<PhotoResponse> photos;

        if (userId != null && status != null) {
            photos = photoService.getUserPhotosByStatus(userId, status, pageable);
        } else if (userId != null) {
            photos = photoService.getUserPhotos(userId, pageable);
        } else if (status != null) {
            photos = photoService.getPhotosByStatus(status, pageable);
        } else {
            // If neither parameter is provided, return empty page or error
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(photos);
    }

    /**
     * DELETE /api/photos/{id} - Delete photo
     */
    @DeleteMapping("/{id}")
    @Timed(value = "photo.delete", description = "Time taken to delete photo")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long id) {
        log.info("Deleting photo: id={}", id);

        photoService.deletePhoto(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * GET /api/photos/{id}/events - Get event log for photo
     */
    @GetMapping("/{id}/events")
    @Timed(value = "photo.events", description = "Time taken to get photo events")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<PhotoEventResponse>> getPhotoEvents(@PathVariable Long id) {
        log.debug("Retrieving events for photo: id={}", id);

        List<PhotoEventResponse> events = photoService.getPhotoEvents(id);
        return ResponseEntity.ok(events);
    }

    /**
     * GET /api/photos/{id}/events (paginated) - Get paginated event log for photo
     */
    @GetMapping("/{id}/events/paginated")
    @Timed(value = "photo.events.paginated", description = "Time taken to get paginated photo events")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<PhotoEventResponse>> getPhotoEventsPaginated(
        @PathVariable Long id,
        @PageableDefault(size = 50, sort = "timestamp") Pageable pageable
    ) {
        log.debug("Retrieving paginated events for photo: id={}, page={}",
            id, pageable.getPageNumber());

        Page<PhotoEventResponse> events = photoService.getPhotoEvents(id, pageable);
        return ResponseEntity.ok(events);
    }

    /**
     * POST /api/photos/{id}/retry - Retry failed photo processing
     */
    @PostMapping("/{id}/retry")
    @Timed(value = "photo.retry", description = "Time taken to retry photo processing")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> retryProcessing(@PathVariable Long id) {
        log.info("Retrying processing for photo: id={}", id);

        photoService.retryProcessing(id);
        return ResponseEntity.accepted().build();
    }

    /**
     * GET /api/photos/stats - Get photo statistics
     */
    @GetMapping("/stats")
    @Timed(value = "photo.stats", description = "Time taken to get photo stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PhotoStatsResponse> getStats() {
        log.debug("Retrieving photo statistics");

        PhotoStatsResponse stats = PhotoStatsResponse.builder()
            .totalPhotos(photoService.countByStatus(PhotoStatus.COMPLETED))
            .pendingPhotos(photoService.countByStatus(PhotoStatus.PENDING))
            .processingPhotos(photoService.countByStatus(PhotoStatus.PROCESSING))
            .failedPhotos(photoService.countByStatus(PhotoStatus.FAILED))
            .build();

        return ResponseEntity.ok(stats);
    }

    /**
     * Photo statistics response
     */
    @lombok.Data
    @lombok.Builder
    public static class PhotoStatsResponse {
        private long totalPhotos;
        private long pendingPhotos;
        private long processingPhotos;
        private long failedPhotos;
    }
}

