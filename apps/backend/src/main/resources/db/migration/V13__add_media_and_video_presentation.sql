-- Link applications to optional video presentation media.
-- Media and transcription tables were introduced in V11; this migration only
-- augments the applications table to reference an uploaded media record.

ALTER TABLE applications
    ADD COLUMN IF NOT EXISTS video_presentation_id BIGINT REFERENCES media(id);

CREATE INDEX IF NOT EXISTS idx_application_video_presentation
    ON applications(video_presentation_id);
