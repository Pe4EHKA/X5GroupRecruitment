-- Add media storage and video presentation support

-- Media table to store uploaded files metadata
CREATE TABLE media (
    id BIGSERIAL PRIMARY KEY,
    storage_key VARCHAR(500) NOT NULL UNIQUE,
    mime_type VARCHAR(100) NOT NULL,
    file_size BIGINT,
    duration INTEGER,
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_media_storage_key ON media(storage_key);
CREATE INDEX idx_media_created_by ON media(created_by);

-- Transcription table linked to media (one-to-one)
CREATE TABLE transcription (
    id BIGSERIAL PRIMARY KEY,
    media_id BIGINT NOT NULL UNIQUE REFERENCES media(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    text TEXT,
    language VARCHAR(10) DEFAULT 'ru',
    segments JSONB,
    error_message TEXT,
    attempts INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transcription_media ON transcription(media_id);
CREATE INDEX idx_transcription_status ON transcription(status);

-- Link applications to optional video presentation media
ALTER TABLE applications
    ADD COLUMN video_presentation_id BIGINT REFERENCES media(id);
