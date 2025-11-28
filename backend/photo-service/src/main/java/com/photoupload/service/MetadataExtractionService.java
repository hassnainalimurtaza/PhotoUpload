package com.photoupload.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.photoupload.common.domain.Photo;
import com.photoupload.storage.CloudStorageProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for extracting photo metadata (EXIF, etc.).
 * Single Responsibility: Handles only metadata extraction.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MetadataExtractionService {

    private final ObjectMapper objectMapper;

    /**
     * Extract metadata from photo
     */
    public String extractMetadata(Photo photo, CloudStorageProvider storage) {
        try {
            log.debug("Extracting metadata for photo: {}", photo.getId());

            // Download image
            InputStream imageStream = storage.download(photo.getStorageKey());

            // Extract metadata
            Metadata metadata = ImageMetadataReader.readMetadata(imageStream);

            // Convert to map
            Map<String, Map<String, String>> metadataMap = new HashMap<>();

            for (Directory directory : metadata.getDirectories()) {
                Map<String, String> tags = new HashMap<>();

                for (Tag tag : directory.getTags()) {
                    tags.put(tag.getTagName(), tag.getDescription());
                }

                if (!tags.isEmpty()) {
                    metadataMap.put(directory.getName(), tags);
                }

                // Log any errors
                if (directory.hasErrors()) {
                    for (String error : directory.getErrors()) {
                        log.warn("Metadata extraction error: {}", error);
                    }
                }
            }

            // Convert to JSON
            String metadataJson = objectMapper.writeValueAsString(metadataMap);

            log.info("Metadata extracted successfully: photoId={}, directories={}",
                photo.getId(), metadataMap.size());

            return metadataJson;

        } catch (Exception e) {
            log.warn("Failed to extract metadata (non-critical): photoId={}, error={}",
                photo.getId(), e.getMessage());
            
            // Return empty JSON on failure (metadata extraction is non-critical)
            return "{}";
        }
    }
}

