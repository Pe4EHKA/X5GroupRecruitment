-- Migration for Vacancy Questionnaire System
-- Adds support for mandatory/optional questions, answers, video recordings, and transcriptions

-- Add optional questions configuration to vacancies
ALTER TABLE vacancies ADD COLUMN optional_questions_to_ask INTEGER DEFAULT 3;

-- Vacancy Questions table
-- Stores questions configured by HR for each vacancy
CREATE TABLE vacancy_question (
    id BIGSERIAL PRIMARY KEY,
    vacancy_id BIGINT NOT NULL REFERENCES vacancies(id) ON DELETE CASCADE,
    text TEXT NOT NULL,
    type VARCHAR(50) NOT NULL, -- TEXT, SINGLE_CHOICE, MULTI_CHOICE, NUMBER, DATE, VIDEO
    mandatory BOOLEAN NOT NULL DEFAULT FALSE,
    random_pool BOOLEAN NOT NULL DEFAULT TRUE, -- For optional questions, include in random selection
    order_index INTEGER NOT NULL DEFAULT 0,
    validation_rules JSONB, -- Stores min/max length, regex, range, allowed values, etc.
    options JSONB, -- For CHOICE types, stores available options
    weight INTEGER DEFAULT 1, -- For future scoring
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_vacancy_question_vacancy ON vacancy_question(vacancy_id);
CREATE INDEX idx_vacancy_question_mandatory ON vacancy_question(mandatory);
CREATE INDEX idx_vacancy_question_order ON vacancy_question(vacancy_id, order_index);

-- Application Questions table
-- Snapshots the questions that were shown to a specific applicant
-- This ensures stability even if vacancy questions change later
CREATE TABLE application_question (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    question_id BIGINT REFERENCES vacancy_question(id) ON DELETE SET NULL,
    text TEXT NOT NULL,
    type VARCHAR(50) NOT NULL,
    mandatory_snapshot BOOLEAN NOT NULL DEFAULT FALSE,
    order_snapshot INTEGER NOT NULL DEFAULT 0,
    validation_rules_snapshot JSONB,
    options_snapshot JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_application_question_application ON application_question(application_id);
CREATE INDEX idx_application_question_question ON application_question(question_id);

-- Media table
-- Stores metadata for uploaded videos and files
CREATE TABLE media (
    id BIGSERIAL PRIMARY KEY,
    storage_key VARCHAR(500) NOT NULL UNIQUE, -- File path or S3 key
    mime_type VARCHAR(100) NOT NULL,
    file_size BIGINT, -- Size in bytes
    duration INTEGER, -- Duration in seconds for video/audio
    created_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_media_storage_key ON media(storage_key);
CREATE INDEX idx_media_created_by ON media(created_by);

-- Application Answers table
-- Stores all types of answers from applicants
CREATE TABLE application_answer (
    id BIGSERIAL PRIMARY KEY,
    application_question_id BIGINT NOT NULL REFERENCES application_question(id) ON DELETE CASCADE,
    text_value TEXT, -- For TEXT type answers
    number_value DECIMAL(15, 2), -- For NUMBER type answers
    date_value DATE, -- For DATE type answers
    choice_values JSONB, -- For SINGLE_CHOICE and MULTI_CHOICE (array of selected options)
    media_id BIGINT REFERENCES media(id) ON DELETE SET NULL, -- For VIDEO type answers
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_application_answer_question ON application_answer(application_question_id);
CREATE INDEX idx_application_answer_media ON application_answer(media_id);

-- Transcription table
-- Stores video transcription data and processing status
CREATE TABLE transcription (
    id BIGSERIAL PRIMARY KEY,
    media_id BIGINT NOT NULL REFERENCES media(id) ON DELETE CASCADE,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING', -- PENDING, PROCESSING, DONE, FAILED
    text TEXT, -- Transcription result
    language VARCHAR(10) DEFAULT 'ru', -- Language code (ru/en)
    segments JSONB, -- Optional: timestamped segments
    error_message TEXT, -- Error details if FAILED
    attempts INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_transcription_media ON transcription(media_id);
CREATE INDEX idx_transcription_status ON transcription(status);
CREATE UNIQUE INDEX idx_transcription_media_unique ON transcription(media_id);

-- Comments
COMMENT ON TABLE vacancy_question IS 'Questions configured by HR for each vacancy';
COMMENT ON TABLE application_question IS 'Snapshot of questions shown to each applicant';
COMMENT ON TABLE media IS 'Metadata for uploaded videos and files';
COMMENT ON TABLE application_answer IS 'Answers provided by applicants';
COMMENT ON TABLE transcription IS 'Video transcription data and processing status';
