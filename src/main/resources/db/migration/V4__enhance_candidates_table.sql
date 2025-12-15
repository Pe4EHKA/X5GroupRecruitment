-- Enhance candidates table with additional fields from Excel import format
-- Version: V4__enhance_candidates_table.sql

-- Add new columns to candidates table
ALTER TABLE candidates 
    ADD COLUMN telegram VARCHAR(100),
    ADD COLUMN phone_e164 VARCHAR(20),
    ADD COLUMN raw_phone VARCHAR(50),
    ADD COLUMN birth_year INTEGER,
    ADD COLUMN citizenship VARCHAR(100),
    ADD COLUMN university VARCHAR(200),
    ADD COLUMN other_university VARCHAR(200),
    ADD COLUMN speciality VARCHAR(200),
    ADD COLUMN other_speciality VARCHAR(200),
    ADD COLUMN course VARCHAR(50),
    ADD COLUMN schedule VARCHAR(100),
    ADD COLUMN city VARCHAR(100),
    ADD COLUMN other_city VARCHAR(100),
    ADD COLUMN source VARCHAR(200),
    ADD COLUMN languages JSONB,
    ADD COLUMN raw_languages TEXT;

-- Create indexes for new fields
CREATE INDEX idx_candidates_phone_e164 ON candidates(phone_e164);
CREATE INDEX idx_candidates_birth_year ON candidates(birth_year);
CREATE INDEX idx_candidates_telegram ON candidates(telegram);

-- Create partial unique index for deduplication
-- Only one non-null email can exist (already handled by UNIQUE constraint)
-- Create partial unique index for phone_e164 when not null
CREATE UNIQUE INDEX idx_candidates_unique_phone_e164 
    ON candidates(phone_e164) 
    WHERE phone_e164 IS NOT NULL;

-- Update existing candidates to have raw_phone from phone
UPDATE candidates SET raw_phone = phone WHERE phone IS NOT NULL;
