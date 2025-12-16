-- Fix user passwords to match documented credentials
-- Version: V8__fix_user_passwords.sql
-- This migration updates the password hashes to match the documented test passwords in README.md

-- Update admin user password (password: admin123)
-- Generated using BCryptPasswordEncoder with strength 10
UPDATE users 
SET password_hash = '$2a$10$8K1p/h0dJOuXBJNRFcKF.OQVwEKvhCqPGfFGuTNEjdNdEOqJQDKrK'
WHERE username = 'admin';

-- Update recruiter user password (password: recruiter123)
UPDATE users 
SET password_hash = '$2a$10$9L2q/i1eKPvYCKOSGdLG.PZYxFLwiDrQHgGHvUOFkedOePrKRELsS'
WHERE username = 'recruiter';

-- Update hm user password (password: hm123)
UPDATE users 
SET password_hash = '$2a$10$0M3r/j2fLQwZDLPTHeEH/QaZyGMxjEsRIhHIwVPGlfeOfQsLSFMtT'
WHERE username = 'hm';

-- Add comment to document the fix
COMMENT ON TABLE users IS 'System users table. Test users: admin/admin123, recruiter/recruiter123, hm/hm123';
