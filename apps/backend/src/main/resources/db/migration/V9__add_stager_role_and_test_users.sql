-- Migration to add STAGER role support and test users
-- This migration adds test users with STAGER role for MVP testing

-- Add test stager user (stager/stager123)
INSERT INTO users (username, email, email_normalized, password_hash, first_name, last_name, status, active, created_at, updated_at)
VALUES 
('stager', 'stager@x5.ru', 'stager@x5.ru', '$2a$10$rXl0p5JqYZ.zVhZ3j7xKJO8C4wR6yU1nKVZ9JK5K4wR6yU1nKVZ9J', 'Иван', 'Стажёр', 'ACTIVE', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (username) DO NOTHING;

-- Add STAGER role to the test user
INSERT INTO user_roles (user_id, role)
SELECT u.id, 'STAGER'
FROM users u
WHERE u.username = 'stager'
ON CONFLICT DO NOTHING;

-- Link stager user to a candidate (if exists)
-- This allows testing the profile and application viewing
UPDATE candidates
SET email = 'stager@x5.ru'
WHERE id = (SELECT MIN(id) FROM candidates LIMIT 1)
AND NOT EXISTS (SELECT 1 FROM candidates WHERE email = 'stager@x5.ru');
