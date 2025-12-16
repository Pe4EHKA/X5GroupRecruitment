-- Fix user passwords to match documented credentials
-- Version: V8__fix_user_passwords.sql
-- This migration updates the password hashes to match the documented test passwords in README.md

-- Update admin user password (password: admin123)
-- Generated using BCryptPasswordEncoder with strength 10
UPDATE users 
SET password_hash = '$2a$10$ly3U/9WO2TzWJsGYr8WREe.IoTbksltexVVZPYZcWNRpa5e2mT7jG'
WHERE username = 'admin';

-- Update recruiter user password (password: recruiter123)
UPDATE users 
SET password_hash = '$2a$10$OX70brqYIzKQLphVNBeFCuFNE0mgX8CJGqiKQjpmfV.fFoKaoAzhS'
WHERE username = 'recruiter';

-- Update hm user password (password: hm123)
UPDATE users 
SET password_hash = '$2a$10$ZGAdXZAA/PLI4QTKbMMPq.10.EEFzQmLaS1S5m4eIOtMQOTlinE5G'
WHERE username = 'hm';

-- Add comment to document the fix
COMMENT ON TABLE users IS 'System users table. Test users: admin/admin123, recruiter/recruiter123, hm/hm123';

