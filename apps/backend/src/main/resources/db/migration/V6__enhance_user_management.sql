-- User Management Enhancement
-- Version: V6__enhance_user_management.sql

-- Add new columns to users table
ALTER TABLE users ADD COLUMN IF NOT EXISTS email_normalized VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS phone VARCHAR(20);
ALTER TABLE users ADD COLUMN IF NOT EXISTS department VARCHAR(100);
ALTER TABLE users ADD COLUMN IF NOT EXISTS comment TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE';
ALTER TABLE users ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP;
ALTER TABLE users ADD COLUMN IF NOT EXISTS created_by BIGINT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS updated_by BIGINT;

-- Populate email_normalized from existing email data
UPDATE users SET email_normalized = LOWER(TRIM(email)) WHERE email_normalized IS NULL;

-- Create unique index on email_normalized
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email_normalized ON users(email_normalized);

-- Create index on status
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

-- Create audit_events table
CREATE TABLE IF NOT EXISTS audit_events (
    id BIGSERIAL PRIMARY KEY,
    actor_user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(50) NOT NULL,
    entity_id BIGINT NOT NULL,
    metadata TEXT,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for audit_events
CREATE INDEX IF NOT EXISTS idx_audit_events_actor ON audit_events(actor_user_id);
CREATE INDEX IF NOT EXISTS idx_audit_events_entity ON audit_events(entity_type, entity_id);
CREATE INDEX IF NOT EXISTS idx_audit_events_timestamp ON audit_events(timestamp);
CREATE INDEX IF NOT EXISTS idx_audit_events_action ON audit_events(action);

-- Add foreign key constraints (optional, for referential integrity)
ALTER TABLE users ADD CONSTRAINT fk_users_created_by 
    FOREIGN KEY (created_by) REFERENCES users(id) ON DELETE SET NULL;
    
ALTER TABLE users ADD CONSTRAINT fk_users_updated_by 
    FOREIGN KEY (updated_by) REFERENCES users(id) ON DELETE SET NULL;

-- Update existing users to have ACTIVE status if they have active=true
UPDATE users SET status = 'ACTIVE' WHERE active = true AND status = 'ACTIVE';
UPDATE users SET status = 'DISABLED' WHERE active = false AND status = 'ACTIVE';
