-- Add application preferences table for priority tracking
-- Version: V5__add_application_preferences_table.sql

-- Application preferences table (for first/second priority)
CREATE TABLE application_preferences (
    id BIGSERIAL PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
    vacancy_id BIGINT REFERENCES vacancies(id),
    rank INTEGER NOT NULL CHECK (rank IN (1, 2)),
    raw_value VARCHAR(200) NOT NULL,
    is_mapped BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(application_id, rank)
);

CREATE INDEX idx_application_preferences_application ON application_preferences(application_id);
CREATE INDEX idx_application_preferences_vacancy ON application_preferences(vacancy_id);
CREATE INDEX idx_application_preferences_rank ON application_preferences(rank);
CREATE INDEX idx_application_preferences_is_mapped ON application_preferences(is_mapped);

-- Add unmapped tracking to vacancies
ALTER TABLE vacancies ADD COLUMN allow_unmapped BOOLEAN NOT NULL DEFAULT TRUE;

-- Add submission date field to applications (from "Дата заявки" in Excel)
ALTER TABLE applications ADD COLUMN submitted_at TIMESTAMP;
