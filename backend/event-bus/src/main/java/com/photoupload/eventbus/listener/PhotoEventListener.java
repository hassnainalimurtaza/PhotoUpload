package com.photoupload.eventbus.listener;

import com.photoupload.common.event.*;

/**
 * Observer Pattern: Interface for observing photo events.
 * Implementations will handle specific event processing logic.
 */
public interface PhotoEventListener {

    /**
     * Handle photo uploaded event
     */
    void onPhotoUploaded(PhotoUploadedEvent event);

    /**
     * Handle photo processing started event
     */
    void onPhotoProcessingStarted(PhotoProcessingStartedEvent event);

    /**
     * Handle photo processing completed event
     */
    void onPhotoProcessingCompleted(PhotoProcessingCompletedEvent event);

    /**
     * Handle photo processing failed event
     */
    void onPhotoProcessingFailed(PhotoProcessingFailedEvent event);

    /**
     * Handle photo deleted event
     */
    void onPhotoDeleted(PhotoDeletedEvent event);
}

