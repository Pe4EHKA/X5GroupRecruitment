# API Usage Examples

## Authentication

All authenticated endpoints use HTTP Basic Authentication. Include credentials with each request:

```bash
# Using curl
curl -u username:password http://localhost:8080/api/...

# Test credentials
# Admin:     admin / admin123
# Recruiter: recruiter / recruiter123
# HM:        hm / hm123
```

## Recruiter API Examples

### 1. List all applications

```bash
curl -u recruiter:recruiter123 \
  http://localhost:8080/api/recruiter/applications
```

### 2. Filter applications by status

```bash
curl -u recruiter:recruiter123 \
  "http://localhost:8080/api/recruiter/applications?status=NEW&page=0&size=20"
```

### 3. Get application details

```bash
curl -u recruiter:recruiter123 \
  http://localhost:8080/api/recruiter/applications/1
```

### 4. Create new application (manual)

```bash
curl -u recruiter:recruiter123 \
  -H "Content-Type: application/json" \
  -X POST \
  -d '{
    "firstName": "Ivan",
    "lastName": "Petrov",
    "email": "ivan.petrov@example.com",
    "phone": "+79001234567",
    "vacancyId": 1,
    "coverLetter": "I am interested in this position",
    "additionalInfo": "3 years Java experience"
  }' \
  http://localhost:8080/api/recruiter/applications
```

### 5. Change application status

```bash
curl -u recruiter:recruiter123 \
  -H "Content-Type: application/json" \
  -X PATCH \
  -d '{
    "newStatus": "SCREENING",
    "comment": "Passed initial review"
  }' \
  http://localhost:8080/api/recruiter/applications/1/status
```

### 6. Send to Hiring Manager

```bash
curl -u recruiter:recruiter123 \
  -X POST \
  http://localhost:8080/api/recruiter/applications/1/send-to-hm
```

## Hiring Manager API Examples

### 1. Get pending applications

```bash
curl -u hm:hm123 \
  http://localhost:8080/api/hm/pending
```

### 2. Make decision on application

```bash
# Approve
curl -u hm:hm123 \
  -H "Content-Type: application/json" \
  -X POST \
  -d '{
    "approved": true,
    "rating": 5,
    "strengths": "Excellent technical skills and communication",
    "weaknesses": "Limited experience in specific frameworks",
    "recommendation": "Strong hire for junior position",
    "generalComments": "Very promising candidate",
    "shareWithCandidate": true
  }' \
  http://localhost:8080/api/hm/applications/1/decision

# Reject
curl -u hm:hm123 \
  -H "Content-Type: application/json" \
  -X POST \
  -d '{
    "approved": false,
    "rating": 2,
    "strengths": "Good educational background",
    "weaknesses": "Lacks required experience",
    "recommendation": "Not suitable for this role",
    "generalComments": "Consider for future opportunities",
    "shareWithCandidate": false
  }' \
  http://localhost:8080/api/hm/applications/2/decision
```

## Candidate API Examples

### 1. Check application status (no auth required)

```bash
curl "http://localhost:8080/api/candidate/status?token=test-token-1"
```

Response:
```json
[
  {
    "candidateName": "Ivan Petrov",
    "vacancyTitle": "Backend Developer Intern",
    "status": "PENDING_HM_REVIEW",
    "statusDescription": "Your application has passed initial screening and is awaiting hiring manager review.",
    "lastUpdated": "2024-01-15T10:30:00",
    "appliedAt": "2024-01-10T14:20:00"
  }
]
```

## Import/Export API Examples

### 1. Import applications from Excel

```bash
curl -u recruiter:recruiter123 \
  -F "file=@applications.xlsx" \
  http://localhost:8080/api/import-export/import
```

### 2. Export approved applications

```bash
curl -u recruiter:recruiter123 \
  -o approved_applications.xlsx \
  http://localhost:8080/api/import-export/export/approved
```

## Excel Import Format

The Excel file should have the following columns (see `docs/import_template.xlsx`):

| Column | Description | Required | Example |
|--------|-------------|----------|---------|
| First Name | Candidate's first name | Yes | Ivan |
| Last Name | Candidate's last name | Yes | Petrov |
| Email | Candidate's email (unique) | Yes | ivan.petrov@example.com |
| Phone | Candidate's phone number | No | +79001234567 |
| Vacancy ID | ID of the vacancy | Yes | 1 |
| Cover Letter | Cover letter text | No | I am very interested... |
| Additional Info | Any additional information | No | 3 years experience in Java |

**Notes:**
- First row should be headers
- Email must be unique (duplicate candidates are merged)
- Vacancy ID must exist in the database
- Phone can be empty or in any format

## Swagger UI

Interactive API documentation is available at:
- http://localhost:8080/swagger-ui.html

You can test all endpoints directly from the browser.

## Common Workflows

### Workflow 1: Complete Recruitment Process

```bash
# 1. Import applications
curl -u recruiter:recruiter123 \
  -F "file=@applications.xlsx" \
  http://localhost:8080/api/import-export/import

# 2. Review and screen
curl -u recruiter:recruiter123 \
  "http://localhost:8080/api/recruiter/applications?status=NEW"

# 3. Change status to screening
curl -u recruiter:recruiter123 \
  -H "Content-Type: application/json" \
  -X PATCH \
  -d '{"newStatus": "SCREENING", "comment": "Under review"}' \
  http://localhost:8080/api/recruiter/applications/1/status

# 4. Send to HM
curl -u recruiter:recruiter123 \
  -X POST \
  http://localhost:8080/api/recruiter/applications/1/send-to-hm

# 5. HM reviews
curl -u hm:hm123 \
  http://localhost:8080/api/hm/pending

# 6. HM makes decision
curl -u hm:hm123 \
  -H "Content-Type: application/json" \
  -X POST \
  -d '{
    "approved": true,
    "rating": 5,
    "strengths": "Great candidate",
    "shareWithCandidate": true
  }' \
  http://localhost:8080/api/hm/applications/1/decision

# 7. Export approved
curl -u recruiter:recruiter123 \
  -o approved.xlsx \
  http://localhost:8080/api/import-export/export/approved
```

### Workflow 2: Candidate Checks Status

```bash
# Candidate receives email with access token and checks status
curl "http://localhost:8080/api/candidate/status?token=abc-123-xyz"
```

## Error Handling

### Validation Error (400)

```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "email": "Email should be valid",
    "firstName": "First name is required"
  },
  "timestamp": "2024-01-15T10:30:00"
}
```

### Not Found (404)

```json
{
  "status": 404,
  "message": "Application not found: 999",
  "timestamp": "2024-01-15T10:30:00"
}
```

### Conflict (409)

```json
{
  "status": 409,
  "message": "Application already exists for this candidate and vacancy",
  "timestamp": "2024-01-15T10:30:00"
}
```

### Forbidden (403)

```json
{
  "status": 403,
  "message": "Access denied",
  "timestamp": "2024-01-15T10:30:00"
}
```

## Health Check

```bash
# Check application health
curl http://localhost:8080/actuator/health

# Response
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP"
    },
    "diskSpace": {
      "status": "UP"
    }
  }
}
```

## Metrics

```bash
# Get all metrics
curl http://localhost:8080/actuator/metrics

# Get specific metric
curl http://localhost:8080/actuator/metrics/jvm.memory.used
```
