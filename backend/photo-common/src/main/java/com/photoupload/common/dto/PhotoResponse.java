package com.photoupload.common.dto;

import com.photoupload.common.domain.PhotoStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Photo response DTO for API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoResponse {

    private Long id;
    private String userId;
    private String originalFileName;
    private String contentType;
    private Long fileSize;
    private String storageUrl;
    private String thumbnailUrl;
    private PhotoStatus status;
    private Integer width;
    private Integer height;
    private String metadata;
    private String checksum;
    private Instant uploadedAt;
    private Instant processedAt;
    private Instant updatedAt;
    private Integer retryCount;
    private String lastError;
}

