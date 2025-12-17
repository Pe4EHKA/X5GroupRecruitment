# User Management Documentation

## Overview

The User Management system allows administrators to manage system users, assign roles, and control access to the X5 Recruitment platform.

## Features

### User Roles

- **ADMIN**: Full system access, including user management
- **RECRUITER**: Access to manage applications and candidates
- **HM** (Hiring Manager): Access to review and make hiring decisions

### User Status

- **ACTIVE**: User can access the system
- **DISABLED**: User cannot access the system (soft delete)
- **INVITED**: User has been invited but hasn't activated their account yet

## API Endpoints

All admin user management endpoints require `ADMIN` role.

### List Users

```http
GET /api/admin/users
```

**Query Parameters:**
- `q` (optional): Search query (email, username, or full name)
- `status` (optional): Filter by status (ACTIVE, DISABLED, INVITED)
- `role` (optional): Filter by role (ADMIN, RECRUITER, HM)
- `page` (optional, default: 0): Page number
- `size` (optional, default: 20): Page size
- `sort` (optional, default: updatedAt): Sort field
- `direction` (optional, default: desc): Sort direction (asc, desc)

**Example:**
```bash
curl -u admin:admin123 \
  "http://localhost:8080/api/admin/users?q=john&status=ACTIVE&page=0&size=20"
```

### Get User by ID

```http
GET /api/admin/users/{id}
```

**Example:**
```bash
curl -u admin:admin123 http://localhost:8080/api/admin/users/1
```

### Create User

```http
POST /api/admin/users
```

**Request Body:**
```json
{
  "username": "newuser",
  "email": "newuser@x5.ru",
  "firstName": "John",
  "lastName": "Doe",
  "phone": "+79001234567",
  "department": "IT",
  "comment": "New developer",
  "roles": ["RECRUITER"],
  "status": "ACTIVE",
  "password": "password123"
}
```

**Example:**
```bash
curl -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{"username":"newuser","email":"newuser@x5.ru","firstName":"John","lastName":"Doe","roles":["RECRUITER"],"status":"ACTIVE","password":"password123"}' \
  http://localhost:8080/api/admin/users
```

### Update User Profile

```http
PUT /api/admin/users/{id}
```

**Request Body:**
```json
{
  "firstName": "Jane",
  "lastName": "Smith",
  "email": "jane.smith@x5.ru",
  "phone": "+79001234567",
  "department": "HR",
  "comment": "Updated profile"
}
```

### Update User Roles

```http
PUT /api/admin/users/{id}/roles
```

**Request Body:**
```json
{
  "roles": ["ADMIN", "RECRUITER"]
}
```

**Note:** This is a full replacement operation. All previous roles will be replaced with the new set.

**Example:**
```bash
curl -u admin:admin123 \
  -X PUT \
  -H "Content-Type: application/json" \
  -d '{"roles":["RECRUITER","HM"]}' \
  http://localhost:8080/api/admin/users/2/roles
```

### Update User Status

```http
PUT /api/admin/users/{id}/status
```

**Request Body:**
```json
{
  "status": "DISABLED"
}
```

**Example:**
```bash
curl -u admin:admin123 \
  -X PUT \
  -H "Content-Type: application/json" \
  -d '{"status":"DISABLED"}' \
  http://localhost:8080/api/admin/users/2/status
```

## UI Access

### Admin User Management Page

Navigate to: `http://localhost:3000/admin/users`

**Features:**
- List all users with filters and search
- Create new users
- Edit user profiles
- Manage user roles
- Enable/disable users
- View user metadata (creation date, last login, etc.)

### Login Credentials (Development)

Default admin account:
- **Username**: `admin`
- **Password**: `admin123`

## Business Rules

### Last Active Admin Protection

- The system prevents disabling the last active admin user
- The system prevents removing the ADMIN role from the last active admin
- If you attempt to do either action, you'll receive a `409 Conflict` error

### Email Normalization

- All emails are normalized to lowercase for case-insensitive lookups
- Duplicate emails are prevented at the database level

### Password Policy

- Minimum password length: 8 characters
- Passwords are hashed using BCrypt
- If no password is provided during user creation, a random one is generated

## Audit Trail

All user management actions are logged in the `audit_events` table:

- `CREATE_USER`: User created
- `UPDATE_USER`: User profile updated
- `UPDATE_ROLES`: User roles changed
- `ENABLE_USER`: User status changed to ACTIVE
- `DISABLE_USER`: User status changed to DISABLED

## Error Codes

- **400 Bad Request**: Validation error
- **401 Unauthorized**: Not authenticated
- **403 Forbidden**: Not authorized (not an admin)
- **404 Not Found**: User not found
- **409 Conflict**: 
  - Username or email already exists
  - Cannot disable last active admin
  - Cannot remove ADMIN role from last active admin

## Testing

### Manual Testing

1. **Create a user:**
   ```bash
   curl -u admin:admin123 \
     -H "Content-Type: application/json" \
     -d '{"username":"testuser","email":"test@x5.ru","firstName":"Test","lastName":"User","roles":["RECRUITER"],"status":"ACTIVE","password":"test123456"}' \
     http://localhost:8080/api/admin/users
   ```

2. **List users:**
   ```bash
   curl -u admin:admin123 http://localhost:8080/api/admin/users
   ```

3. **Update user roles:**
   ```bash
   curl -u admin:admin123 \
     -X PUT \
     -H "Content-Type: application/json" \
     -d '{"roles":["RECRUITER","HM"]}' \
     http://localhost:8080/api/admin/users/4/roles
   ```

4. **Disable user:**
   ```bash
   curl -u admin:admin123 \
     -X PUT \
     -H "Content-Type: application/json" \
     -d '{"status":"DISABLED"}' \
     http://localhost:8080/api/admin/users/4/status
   ```

5. **Verify disabled user cannot login:**
   ```bash
   curl -u testuser:test123456 http://localhost:8080/api/admin/users
   # Should return 401 Unauthorized
   ```

### UI Testing

1. Log in as admin (admin/admin123)
2. Navigate to `/admin/users`
3. Create a new user with RECRUITER role
4. Open the user details page
5. Update the user's roles (add HM role)
6. Disable the user
7. Verify the user appears as DISABLED in the list

## Database Schema

### users table

- `id`: BIGSERIAL PRIMARY KEY
- `username`: VARCHAR(100) UNIQUE NOT NULL
- `email`: VARCHAR(255) UNIQUE NOT NULL
- `email_normalized`: VARCHAR(255) UNIQUE (for case-insensitive lookups)
- `password_hash`: VARCHAR(255) NOT NULL
- `first_name`: VARCHAR(100) NOT NULL
- `last_name`: VARCHAR(100) NOT NULL
- `phone`: VARCHAR(20)
- `department`: VARCHAR(100)
- `comment`: TEXT
- `status`: VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
- `active`: BOOLEAN NOT NULL DEFAULT TRUE (deprecated, kept for backward compatibility)
- `last_login_at`: TIMESTAMP
- `created_by`: BIGINT (FK to users.id)
- `updated_by`: BIGINT (FK to users.id)
- `created_at`: TIMESTAMP NOT NULL
- `updated_at`: TIMESTAMP NOT NULL

### user_roles table

- `user_id`: BIGINT NOT NULL (FK to users.id)
- `role`: VARCHAR(50) NOT NULL
- PRIMARY KEY (user_id, role)

### audit_events table

- `id`: BIGSERIAL PRIMARY KEY
- `actor_user_id`: BIGINT (FK to users.id, nullable)
- `action`: VARCHAR(100) NOT NULL
- `entity_type`: VARCHAR(50) NOT NULL
- `entity_id`: BIGINT NOT NULL
- `metadata`: TEXT (JSON)
- `timestamp`: TIMESTAMP NOT NULL

## Future Enhancements (Out of Scope)

- Password reset functionality
- User invitation emails
- Two-factor authentication
- Password expiration policy
- User session management
- Advanced audit log viewer UI
- Bulk user operations
