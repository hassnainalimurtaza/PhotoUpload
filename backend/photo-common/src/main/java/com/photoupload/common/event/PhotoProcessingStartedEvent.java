package com.photoupload.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Event published when photo processing starts
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoProcessingStartedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long photoId;
    private String userId;
    private String correlationId;
    private Instant timestamp;

    public static PhotoProcessingStartedEvent of(Long photoId, String userId, String correlationId) {
        return PhotoProcessingStartedEvent.builder()
            .photoId(photoId)
            .userId(userId)
            .correlationId(correlationId)
            .timestamp(Instant.now())
            .build();
    }
}

