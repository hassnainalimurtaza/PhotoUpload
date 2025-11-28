package com.photoupload.common.domain;

import java.util.EnumSet;
import java.util.Set;

/**
 * Photo processing status implementing State pattern.
 * Defines valid state transitions in the photo processing lifecycle.
 */
public enum PhotoStatus {
    PENDING,
    UPLOADING,
    UPLOADED,
    PROCESSING,
    RETRYING,
    COMPLETED,
    FAILED;

    /**
     * Check if transition to new status is allowed
     */
    public boolean canTransitionTo(PhotoStatus newStatus) {
        return switch (this) {
            case PENDING -> newStatus == UPLOADING || newStatus == FAILED;
            case UPLOADING -> newStatus == UPLOADED || newStatus == FAILED;
            case UPLOADED -> newStatus == PROCESSING || newStatus == FAILED;
            case PROCESSING -> newStatus == COMPLETED || newStatus == FAILED || newStatus == RETRYING;
            case RETRYING -> newStatus == PROCESSING || newStatus == FAILED;
            case COMPLETED -> false;
            case FAILED -> newStatus == RETRYING || newStatus == PENDING;
        };
    }

    /**
     * Get all allowed transitions from current status
     */
    public Set<PhotoStatus> getAllowedTransitions() {
        return switch (this) {
            case PENDING -> EnumSet.of(UPLOADING, FAILED);
            case UPLOADING -> EnumSet.of(UPLOADED, FAILED);
            case UPLOADED -> EnumSet.of(PROCESSING, FAILED);
            case PROCESSING -> EnumSet.of(COMPLETED, FAILED, RETRYING);
            case RETRYING -> EnumSet.of(PROCESSING, FAILED);
            case COMPLETED -> EnumSet.noneOf(PhotoStatus.class);
            case FAILED -> EnumSet.of(RETRYING, PENDING);
        };
    }

    /**
     * Check if this is a terminal state
     */
    public boolean isTerminal() {
        return this == COMPLETED;
    }

    /**
     * Check if this is an error state
     */
    public boolean isError() {
        return this == FAILED;
    }

    /**
     * Check if processing is in progress
     */
    public boolean isInProgress() {
        return this == UPLOADING || this == PROCESSING || this == RETRYING;
    }
}

