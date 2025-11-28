package com.photoupload.service.mapper;

import com.photoupload.common.domain.Photo;
import com.photoupload.common.domain.PhotoEvent;
import com.photoupload.common.dto.PhotoEventResponse;
import com.photoupload.common.dto.PhotoResponse;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between entities and DTOs.
 * Single Responsibility: Handles only object mapping.
 */
@Component
public class PhotoMapper {

    /**
     * Convert Photo entity to PhotoResponse DTO
     */
    public PhotoResponse toResponse(Photo photo) {
        if (photo == null) {
            return null;
        }

        return PhotoResponse.builder()
            .id(photo.getId())
            .userId(photo.getUserId())
            .originalFileName(photo.getOriginalFileName())
            .contentType(photo.getContentType())
            .fileSize(photo.getFileSize())
            .storageUrl(photo.getStorageUrl())
            .thumbnailUrl(photo.getThumbnailUrl())
            .status(photo.getStatus())
            .width(photo.getWidth())
            .height(photo.getHeight())
            .metadata(photo.getMetadata())
            .checksum(photo.getChecksum())
            .uploadedAt(photo.getUploadedAt())
            .processedAt(photo.getProcessedAt())
            .updatedAt(photo.getUpdatedAt())
            .retryCount(photo.getRetryCount())
            .lastError(photo.getLastError())
            .build();
    }

    /**
     * Convert PhotoEvent entity to PhotoEventResponse DTO
     */
    public PhotoEventResponse toEventResponse(PhotoEvent event) {
        if (event == null) {
            return null;
        }

        return PhotoEventResponse.builder()
            .id(event.getId())
            .photoId(event.getPhotoId())
            .eventType(event.getEventType())
            .timestamp(event.getTimestamp())
            .details(event.getDetails())
            .userId(event.getUserId())
            .correlationId(event.getCorrelationId())
            .sourceService(event.getSourceService())
            .success(event.getSuccess())
            .errorMessage(event.getErrorMessage())
            .build();
    }
}

