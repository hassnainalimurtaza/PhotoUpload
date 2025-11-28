package com.photoupload.common.exception;

/**
 * Exception thrown when storage operations fail
 */
public class StorageException extends RuntimeException {

    private final String storageProvider;
    private final String operation;

    public StorageException(String message) {
        super(message);
        this.storageProvider = null;
        this.operation = null;
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
        this.storageProvider = null;
        this.operation = null;
    }

    public StorageException(String storageProvider, String operation, String message, Throwable cause) {
        super(String.format("[%s] Failed to %s: %s", storageProvider, operation, message), cause);
        this.storageProvider = storageProvider;
        this.operation = operation;
    }

    public String getStorageProvider() {
        return storageProvider;
    }

    public String getOperation() {
        return operation;
    }
}

