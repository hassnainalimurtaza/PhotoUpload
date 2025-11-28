package com.photoupload.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Event published when photo is successfully uploaded to storage
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoUploadedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long photoId;
    private String userId;
    private String storageKey;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private String correlationId;
    private Instant timestamp;

    /**
     * Factory method
     */
    public static PhotoUploadedEvent of(Long photoId, String userId, String storageKey,
                                        String originalFileName, String contentType,
                                        Long fileSize, String correlationId) {
        return PhotoUploadedEvent.builder()
            .photoId(photoId)
            .userId(userId)
            .storageKey(storageKey)
            .originalFileName(originalFileName)
            .contentType(contentType)
            .fileSize(fileSize)
            .correlationId(correlationId)
            .timestamp(Instant.now())
            .build();
    }
}

