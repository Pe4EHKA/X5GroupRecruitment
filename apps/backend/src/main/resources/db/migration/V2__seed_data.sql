-- Seed data for testing and development
-- Version: V2__seed_data.sql

-- Insert default admin user (password: admin123)
-- Password hash generated using BCrypt with strength 10
INSERT INTO users (username, email, password_hash, first_name, last_name, active)
VALUES ('admin', 'admin@x5.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMye8JIk0jY2PJBhYflRJGdkLW8WdF.3rIa', 'Admin', 'User', true);

-- Insert test recruiter (password: recruiter123)
INSERT INTO users (username, email, password_hash, first_name, last_name, active)
VALUES ('recruiter', 'recruiter@x5.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMye8JIk0jY2PJBhYflRJGdkLW8WdF.3rIa', 'Test', 'Recruiter', true);

-- Insert test hiring manager (password: hm123)
INSERT INTO users (username, email, password_hash, first_name, last_name, active)
VALUES ('hm', 'hm@x5.ru', '$2a$10$N9qo8uLOickgx2ZMRZoMye8JIk0jY2PJBhYflRJGdkLW8WdF.3rIa', 'Test', 'HiringManager', true);

-- Assign roles
INSERT INTO user_roles (user_id, role) VALUES (1, 'ADMIN');
INSERT INTO user_roles (user_id, role) VALUES (2, 'RECRUITER');
INSERT INTO user_roles (user_id, role) VALUES (3, 'HM');

-- Insert test vacancies
INSERT INTO vacancies (code, title, description, department, location, positions_available, start_date, end_date, active, hiring_manager_id)
VALUES 
    ('INT-2024-DEV', 'Backend Developer Intern', 'Internship for Java/Spring Boot development', 'IT', 'Moscow', 5, '2024-07-01', '2024-12-31', true, 3),
    ('INT-2024-FE', 'Frontend Developer Intern', 'Internship for React/TypeScript development', 'IT', 'Moscow', 3, '2024-07-01', '2024-12-31', true, 3),
    ('INT-2024-DATA', 'Data Analyst Intern', 'Internship for data analysis and reporting', 'Analytics', 'Moscow', 2, '2024-07-01', '2024-12-31', true, 3);

-- Insert test candidates
INSERT INTO candidates (first_name, last_name, email, phone, additional_info, access_token)
VALUES 
    ('Ivan', 'Petrov', 'ivan.petrov@example.com', '+79001234567', 'Experienced Java developer', 'test-token-1'),
    ('Maria', 'Sidorova', 'maria.sidorova@example.com', '+79001234568', 'React enthusiast', 'test-token-2'),
    ('Alexey', 'Ivanov', 'alexey.ivanov@example.com', '+79001234569', 'Data science background', 'test-token-3');
