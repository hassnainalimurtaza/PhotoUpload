-- Create photos table
CREATE TABLE IF NOT EXISTS photos (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(500) NOT NULL,
    content_type VARCHAR(100),
    file_size BIGINT,
    storage_key VARCHAR(500) UNIQUE,
    storage_url VARCHAR(1000),
    thumbnail_url VARCHAR(1000),
    status VARCHAR(50) NOT NULL,
    width INTEGER,
    height INTEGER,
    metadata TEXT,
    checksum VARCHAR(64),
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0,
    retry_count INTEGER DEFAULT 0,
    last_error TEXT,
    CONSTRAINT photos_status_check CHECK (status IN ('PENDING', 'UPLOADING', 'UPLOADED', 'PROCESSING', 'RETRYING', 'COMPLETED', 'FAILED'))
);

-- Create indexes
CREATE INDEX idx_photos_user_id ON photos(user_id);
CREATE INDEX idx_photos_status ON photos(status);
CREATE INDEX idx_photos_uploaded_at ON photos(uploaded_at);
CREATE INDEX idx_photos_checksum ON photos(checksum);

-- Add comments
COMMENT ON TABLE photos IS 'Main table for storing photo metadata and processing status';
COMMENT ON COLUMN photos.user_id IS 'ID of the user who uploaded the photo';
COMMENT ON COLUMN photos.storage_key IS 'Unique key for the photo in cloud storage';
COMMENT ON COLUMN photos.status IS 'Current processing status of the photo';
COMMENT ON COLUMN photos.version IS 'Optimistic locking version for concurrent updates';

