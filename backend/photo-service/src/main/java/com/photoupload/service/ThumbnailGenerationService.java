package com.photoupload.service;

import com.photoupload.common.domain.Photo;
import com.photoupload.common.exception.PhotoProcessingException;
import com.photoupload.storage.CloudStorageProvider;
import lombok.extern.slf4j.Slf4j;
import org.imgscalr.Scalr;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * Service for generating photo thumbnails.
 * Single Responsibility: Handles only thumbnail generation.
 */
@Slf4j
@Service
public class ThumbnailGenerationService {

    private static final int THUMBNAIL_WIDTH = 300;
    private static final int THUMBNAIL_HEIGHT = 300;
    private static final String THUMBNAIL_FORMAT = "jpg";

    /**
     * Generate thumbnail for photo
     */
    public String generateThumbnail(Photo photo, CloudStorageProvider storage) {
        try {
            log.debug("Generating thumbnail for photo: {}", photo.getId());

            // Download original image
            InputStream imageStream = storage.download(photo.getStorageKey());
            BufferedImage originalImage = ImageIO.read(imageStream);

            if (originalImage == null) {
                throw new PhotoProcessingException(photo.getId(), "thumbnail",
                    "Failed to read image file");
            }

            // Update photo dimensions
            photo.setWidth(originalImage.getWidth());
            photo.setHeight(originalImage.getHeight());

            // Generate thumbnail
            BufferedImage thumbnail = Scalr.resize(
                originalImage,
                Scalr.Method.QUALITY,
                Scalr.Mode.FIT_TO_WIDTH,
                THUMBNAIL_WIDTH,
                THUMBNAIL_HEIGHT,
                Scalr.OP_ANTIALIAS
            );

            // Convert to byte array
            ByteArrayOutputStream thumbnailStream = new ByteArrayOutputStream();
            ImageIO.write(thumbnail, THUMBNAIL_FORMAT, thumbnailStream);
            byte[] thumbnailBytes = thumbnailStream.toByteArray();

            // Generate thumbnail storage key
            String thumbnailKey = generateThumbnailKey(photo.getStorageKey());

            // Upload thumbnail to storage
            String thumbnailUrl = storage.upload(
                thumbnailKey,
                new ByteArrayInputStream(thumbnailBytes),
                "image/jpeg",
                thumbnailBytes.length
            );

            log.info("Thumbnail generated successfully: photoId={}, size={}x{}",
                photo.getId(), thumbnail.getWidth(), thumbnail.getHeight());

            return thumbnailUrl;

        } catch (Exception e) {
            log.error("Failed to generate thumbnail: photoId={}", photo.getId(), e);
            throw new PhotoProcessingException(photo.getId(), "thumbnail",
                "Thumbnail generation failed: " + e.getMessage(), e);
        }
    }

    /**
     * Generate thumbnail storage key from original key
     */
    private String generateThumbnailKey(String originalKey) {
        int lastSlash = originalKey.lastIndexOf('/');
        int lastDot = originalKey.lastIndexOf('.');

        String path = originalKey.substring(0, lastSlash + 1);
        String filename = originalKey.substring(lastSlash + 1, lastDot);
        String extension = originalKey.substring(lastDot);

        return path + filename + "_thumb" + extension;
    }
}

