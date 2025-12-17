-- Fix stager user password to match documented credentials
-- Version: V10__fix_stager_password.sql

-- Update stager user password (password: stager123)
-- Generated using Python bcrypt library with cost factor 10
-- Note: $2b$ prefix is compatible with Spring Security BCryptPasswordEncoder
UPDATE users 
SET password_hash = '$2b$10$p6PnpVwBDzrIVXB.mdhPe.P/ld0hsjXUFsgk6r7V7jql4vj6pLQnG'
WHERE username = 'stager';
