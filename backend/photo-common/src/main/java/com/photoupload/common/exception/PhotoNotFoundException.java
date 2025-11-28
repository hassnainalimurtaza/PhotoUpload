package com.photoupload.common.exception;

/**
 * Exception thrown when a photo is not found
 */
public class PhotoNotFoundException extends RuntimeException {

    private final Long photoId;

    public PhotoNotFoundException(Long photoId) {
        super(String.format("Photo not found with id: %d", photoId));
        this.photoId = photoId;
    }

    public PhotoNotFoundException(String message) {
        super(message);
        this.photoId = null;
    }

    public Long getPhotoId() {
        return photoId;
    }
}

