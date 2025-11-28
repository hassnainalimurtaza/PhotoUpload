-- Create photo_events table for event sourcing
CREATE TABLE IF NOT EXISTS photo_events (
    id BIGSERIAL PRIMARY KEY,
    photo_id BIGINT NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details TEXT,
    user_id VARCHAR(255),
    correlation_id VARCHAR(100),
    source_service VARCHAR(100),
    success BOOLEAN,
    error_message TEXT,
    CONSTRAINT fk_photo_events_photo FOREIGN KEY (photo_id) REFERENCES photos(id) ON DELETE CASCADE
);

-- Create indexes
CREATE INDEX idx_photo_events_photo_id ON photo_events(photo_id);
CREATE INDEX idx_photo_events_event_type ON photo_events(event_type);
CREATE INDEX idx_photo_events_timestamp ON photo_events(timestamp);
CREATE INDEX idx_photo_events_correlation_id ON photo_events(correlation_id);

-- Add comments
COMMENT ON TABLE photo_events IS 'Event sourcing table for tracking photo processing workflow';
COMMENT ON COLUMN photo_events.event_type IS 'Type of event (e.g., PHOTO_UPLOADED, PHOTO_PROCESSING_STARTED)';
COMMENT ON COLUMN photo_events.correlation_id IS 'ID for tracing related events across the system';

