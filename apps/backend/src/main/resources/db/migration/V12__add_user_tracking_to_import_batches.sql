-- Migration to add user account tracking to import batches
-- Tracks how many user accounts were created or linked during Excel import

ALTER TABLE import_batches ADD COLUMN users_created INTEGER NOT NULL DEFAULT 0;
ALTER TABLE import_batches ADD COLUMN users_linked INTEGER NOT NULL DEFAULT 0;

COMMENT ON COLUMN import_batches.users_created IS 'Number of new user accounts created during import';
COMMENT ON COLUMN import_batches.users_linked IS 'Number of existing user accounts linked during import';
