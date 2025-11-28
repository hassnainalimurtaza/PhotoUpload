package com.photoupload.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Event published when photo processing completes successfully
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoProcessingCompletedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long photoId;
    private String userId;
    private String thumbnailUrl;
    private Integer width;
    private Integer height;
    private String metadata;
    private String correlationId;
    private Instant timestamp;

    public static PhotoProcessingCompletedEvent of(Long photoId, String userId,
                                                   String thumbnailUrl, Integer width,
                                                   Integer height, String metadata,
                                                   String correlationId) {
        return PhotoProcessingCompletedEvent.builder()
            .photoId(photoId)
            .userId(userId)
            .thumbnailUrl(thumbnailUrl)
            .width(width)
            .height(height)
            .metadata(metadata)
            .correlationId(correlationId)
            .timestamp(Instant.now())
            .build();
    }
}

