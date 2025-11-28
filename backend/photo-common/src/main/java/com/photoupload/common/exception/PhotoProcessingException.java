package com.photoupload.common.exception;

/**
 * Exception thrown when photo processing fails
 */
public class PhotoProcessingException extends RuntimeException {

    private final Long photoId;
    private final String processingStage;

    public PhotoProcessingException(Long photoId, String processingStage, String message) {
        super(String.format("Processing failed for photo %d at stage %s: %s", photoId, processingStage, message));
        this.photoId = photoId;
        this.processingStage = processingStage;
    }

    public PhotoProcessingException(Long photoId, String processingStage, String message, Throwable cause) {
        super(String.format("Processing failed for photo %d at stage %s: %s", photoId, processingStage, message), cause);
        this.photoId = photoId;
        this.processingStage = processingStage;
    }

    public Long getPhotoId() {
        return photoId;
    }

    public String getProcessingStage() {
        return processingStage;
    }
}

