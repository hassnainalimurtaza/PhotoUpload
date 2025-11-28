package com.photoupload.common.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.Instant;

/**
 * Event published when photo processing fails
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoProcessingFailedEvent implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long photoId;
    private String userId;
    private String errorMessage;
    private String errorType;
    private Integer retryCount;
    private Boolean willRetry;
    private String correlationId;
    private Instant timestamp;

    public static PhotoProcessingFailedEvent of(Long photoId, String userId,
                                                String errorMessage, String errorType,
                                                Integer retryCount, Boolean willRetry,
                                                String correlationId) {
        return PhotoProcessingFailedEvent.builder()
            .photoId(photoId)
            .userId(userId)
            .errorMessage(errorMessage)
            .errorType(errorType)
            .retryCount(retryCount)
            .willRetry(willRetry)
            .correlationId(correlationId)
            .timestamp(Instant.now())
            .build();
    }
}

