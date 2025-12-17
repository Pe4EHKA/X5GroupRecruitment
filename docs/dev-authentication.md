# Development Authentication Guide

## Overview

This guide explains how to authenticate during development and testing.

## Default Test Users

The system comes with three pre-configured test users for development:

| Username   | Password      | Role      | Description                  |
|------------|---------------|-----------|------------------------------|
| `admin`    | `admin123`    | ADMIN     | Full system access           |
| `recruiter`| `recruiter123`| RECRUITER | Recruiter workflow access    |
| `hm`       | `hm123`       | HM        | Hiring Manager access        |

## Authentication Method

The system uses **HTTP Basic Authentication** for the MVP.

### Example curl requests:

```bash
# Admin - List all users
curl -u admin:admin123 http://localhost:8080/api/admin/users?page=0&size=20

# Recruiter - List applications
curl -u recruiter:recruiter123 http://localhost:8080/api/recruiter/applications?page=0&size=20

# HM - View pending applications
curl -u hm:hm123 http://localhost:8080/api/hm/pending?page=0&size=20

# Health check (public endpoint)
curl http://localhost:8080/actuator/health
```

## Development Mode

### Automatic User Initialization

When running with the `dev` or `default` profile, the `DevDataInitializer` automatically ensures test users exist with correct passwords.

### Reset Passwords (Dev Only)

If you need to reset test user passwords during development:

```bash
# Set environment variable
export APP_DEV_RESET_PASSWORDS=true

# Or in application.properties
app.dev.reset-passwords=true

# Then restart the application
make dev
```

**⚠️ Warning**: This will reset ALL test user passwords to their default values. Only use in development!

### Manual Password Reset (Production)

For production environments, use the admin API to reset user passwords:

```bash
# Create a new user with a specific password
curl -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@x5.ru",
    "password": "securePassword123!",
    "firstName": "New",
    "lastName": "User",
    "roles": ["RECRUITER"],
    "status": "ACTIVE"
  }' \
  http://localhost:8080/api/admin/users
```

## Password Security

### Encoding

All passwords are hashed using **BCrypt** with strength 10:

- Passwords are NEVER stored in plain text
- Each password gets a unique salt
- BCrypt is computationally expensive to resist brute-force attacks

### Migration

The database migration `V8__fix_user_passwords.sql` ensures test users have correct password hashes:

```sql
-- admin password: admin123
UPDATE users SET password_hash = '$2a$10$...' WHERE username = 'admin';

-- recruiter password: recruiter123
UPDATE users SET password_hash = '$2a$10$...' WHERE username = 'recruiter';

-- hm password: hm123
UPDATE users SET password_hash = '$2a$10$...' WHERE username = 'hm';
```

## Troubleshooting

### "Bad credentials" error

If you get authentication errors:

1. **Verify you're using the correct password**:
   - admin: `admin123`
   - recruiter: `recruiter123`
   - hm: `hm123`

2. **Check database migrations were applied**:
   ```bash
   # Connect to database
   docker-compose exec postgres psql -U recruitment -d recruitment
   
   # Check flyway schema history
   SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;
   
   # Should show V8__fix_user_passwords.sql
   ```

3. **Verify user exists and is active**:
   ```sql
   SELECT username, email, status, active FROM users WHERE username = 'admin';
   ```

4. **Reset passwords using dev initializer**:
   ```bash
   export APP_DEV_RESET_PASSWORDS=true
   make dev
   ```

### Check password hash

You can verify a password hash matches using SQL:

```sql
-- Note: This doesn't actually verify the hash, just shows it
SELECT username, password_hash FROM users WHERE username = 'admin';
```

To properly verify, use the application's PasswordEncoder in a test.

## Testing Authentication

Run the authentication integration tests:

```bash
cd apps/backend
mvn test -Dtest=AuthenticationIntegrationTest
mvn test -Dtest=PasswordEncoderIntegrationTest
```

These tests verify:
- Password encoding works correctly
- Authentication succeeds with correct credentials
- Authentication fails with wrong credentials
- Inactive users cannot authenticate

## Security Best Practices

### For Development
- ✅ Use the default test credentials
- ✅ Run dev initializer to ensure users exist
- ✅ Use Basic Auth (acceptable for local development)

### For Production
- ❌ NEVER use test credentials (admin123, etc.)
- ✅ Create strong, unique passwords for all users
- ✅ Consider migrating to OAuth2/JWT
- ✅ Enable HTTPS
- ✅ Disable dev initializer (`spring.profiles.active=prod`)
- ✅ Set `app.dev.reset-passwords=false` or don't set it at all

## Related Documentation

- [README.md](../README.md) - General project documentation
- [docs/admin-user-management.md](admin-user-management.md) - User management API
- [docs/runbook.md](runbook.md) - Deployment and operations guide
