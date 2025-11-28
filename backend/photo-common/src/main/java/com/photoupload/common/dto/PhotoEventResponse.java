package com.photoupload.common.dto;

import com.photoupload.common.domain.PhotoEvent.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Photo event response DTO for workflow tracking
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoEventResponse {

    private Long id;
    private Long photoId;
    private EventType eventType;
    private Instant timestamp;
    private String details;
    private String userId;
    private String correlationId;
    private String sourceService;
    private Boolean success;
    private String errorMessage;
}

