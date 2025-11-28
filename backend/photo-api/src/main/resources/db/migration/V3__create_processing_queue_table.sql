-- Create processing_queue table for database fallback
CREATE TABLE IF NOT EXISTS processing_queue (
    id BIGSERIAL PRIMARY KEY,
    photo_id BIGINT NOT NULL,
    command_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    retry_count INTEGER NOT NULL DEFAULT 0,
    max_retries INTEGER NOT NULL DEFAULT 3,
    next_retry_at TIMESTAMP,
    last_error TEXT,
    payload TEXT,
    correlation_id VARCHAR(100),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    CONSTRAINT fk_processing_queue_photo FOREIGN KEY (photo_id) REFERENCES photos(id) ON DELETE CASCADE,
    CONSTRAINT processing_queue_command_type_check CHECK (command_type IN ('PROCESS_PHOTO', 'GENERATE_THUMBNAIL', 'EXTRACT_METADATA', 'VALIDATE_PHOTO', 'DELETE_PHOTO')),
    CONSTRAINT processing_queue_status_check CHECK (status IN ('PENDING', 'PROCESSING', 'COMPLETED', 'FAILED', 'DEAD_LETTER'))
);

-- Create indexes
CREATE INDEX idx_processing_queue_status ON processing_queue(status);
CREATE INDEX idx_processing_queue_next_retry_at ON processing_queue(next_retry_at);
CREATE INDEX idx_processing_queue_photo_id ON processing_queue(photo_id);

-- Add comments
COMMENT ON TABLE processing_queue IS 'Database-backed queue for fallback when message queue is unavailable';
COMMENT ON COLUMN processing_queue.command_type IS 'Type of processing command to execute';
COMMENT ON COLUMN processing_queue.next_retry_at IS 'Timestamp for next retry attempt with exponential backoff';

