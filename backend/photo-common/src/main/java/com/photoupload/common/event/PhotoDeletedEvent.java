package com.photoupload.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Event published when photo is deleted
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDeletedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long photoId;
    private String userId;
    private String storageKey;
    private String correlationId;
    private Instant timestamp;

    public static PhotoDeletedEvent of(Long photoId, String userId, String storageKey, String correlationId) {
        return PhotoDeletedEvent.builder()
            .photoId(photoId)
            .userId(userId)
            .storageKey(storageKey)
            .correlationId(correlationId)
            .timestamp(Instant.now())
            .build();
    }
}

