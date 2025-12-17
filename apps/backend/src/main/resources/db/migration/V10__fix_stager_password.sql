-- Fix stager user password to match documented credentials
-- Version: V10__fix_stager_password.sql

-- Update stager user password (password: stager123)
-- Generated using BCryptPasswordEncoder with strength 10
UPDATE users 
SET password_hash = '$2b$10$p6PnpVwBDzrIVXB.mdhPe.P/ld0hsjXUFsgk6r7V7jql4vj6pLQnG'
WHERE username = 'stager';
