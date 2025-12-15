-- Add import tracking tables for batch processing and error reporting
-- Version: V3__add_import_tracking_tables.sql

-- Import batches table
CREATE TABLE import_batches (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(500) NOT NULL,
    uploaded_by_id BIGINT REFERENCES users(id),
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total_rows INTEGER NOT NULL DEFAULT 0,
    success_rows INTEGER NOT NULL DEFAULT 0,
    failed_rows INTEGER NOT NULL DEFAULT 0,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_import_batches_uploaded_at ON import_batches(uploaded_at);
CREATE INDEX idx_import_batches_uploaded_by ON import_batches(uploaded_by_id);
CREATE INDEX idx_import_batches_completed ON import_batches(completed);

-- Import row errors table
CREATE TABLE import_row_errors (
    id BIGSERIAL PRIMARY KEY,
    batch_id BIGINT NOT NULL REFERENCES import_batches(id) ON DELETE CASCADE,
    row_number INTEGER NOT NULL,
    error_code VARCHAR(100) NOT NULL,
    error_message TEXT NOT NULL,
    raw_snapshot JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_import_row_errors_batch ON import_row_errors(batch_id);
CREATE INDEX idx_import_row_errors_error_code ON import_row_errors(error_code);
