-- Update seed data for user management enhancement
-- Version: V7__update_seed_data_for_user_management.sql

-- Update existing users to have proper normalized emails and status
UPDATE users SET 
  email_normalized = LOWER(TRIM(email)),
  status = 'ACTIVE'
WHERE email_normalized IS NULL OR status IS NULL;

-- Add audit event for initial setup (optional)
INSERT INTO audit_events (actor_user_id, action, entity_type, entity_id, metadata, timestamp)
SELECT 
  NULL,
  'CREATE_USER',
  'USER',
  id,
  CONCAT('Initial seed user: ', username),
  created_at
FROM users
WHERE id IN (1, 2, 3);
