package com.photoupload.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Storage metadata for files in cloud storage
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StorageMetadata {

    private String key;
    private String contentType;
    private Long contentLength;
    private String etag;
    private Instant lastModified;
    private Map<String, String> userMetadata;
    private String storageClass;
    private String encryption;
}

