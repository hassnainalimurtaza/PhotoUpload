package com.photoupload.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

/**
 * Photo upload request DTO with validation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoUploadRequest {

    @NotNull(message = "File is required")
    private MultipartFile file;

    @NotBlank(message = "User ID is required")
    private String userId;

    private String description;

    private String tags;

    /**
     * Validate file properties
     */
    public void validate() {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        // Max file size: 50MB
        long maxSize = 50 * 1024 * 1024;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 50MB");
        }
    }

    /**
     * Get original filename
     */
    public String getOriginalFilename() {
        return file != null ? file.getOriginalFilename() : null;
    }

    /**
     * Get content type
     */
    public String getContentType() {
        return file != null ? file.getContentType() : null;
    }

    /**
     * Get file size
     */
    public long getFileSize() {
        return file != null ? file.getSize() : 0;
    }
}

