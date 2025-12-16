# Smoke Testing Guide

This document describes how to run smoke tests on the X5 Recruitment System backend to verify all key features are working.

## Prerequisites

- Backend must be running on `http://localhost:8080`
- Database must be up and running
- Test data must be seeded (happens automatically on first run)

## Running Smoke Tests

### Automated Smoke Test Script

A comprehensive smoke test script is available in `docs/smoke.sh`:

```bash
chmod +x docs/smoke.sh
./docs/smoke.sh
```

### Manual Smoke Tests

You can also run smoke tests manually using curl commands:

#### Part 1: Infrastructure Endpoints

**Test 1.1: Health Check**
```bash
curl -s http://localhost:8080/actuator/health
# Expected: {"status":"UP"}
```

**Test 1.2: OpenAPI Documentation**
```bash
curl -s http://localhost:8080/api-docs | jq .info.title
# Expected: "X5 Recruitment System API"
```

**Test 1.3: Swagger UI**
```bash
curl -I http://localhost:8080/swagger-ui.html
# Expected: HTTP 302 (redirect to swagger-ui/index.html)
```

#### Part 2: Recruiter Endpoints

**Test 2.1: Dashboard Metrics**
```bash
curl -u recruiter:admin123 http://localhost:8080/api/recruiter/dashboard/metrics
# Expected: JSON with totalApplications, byStatus, etc.
```

**Test 2.2: List Applications**
```bash
curl -u recruiter:admin123 "http://localhost:8080/api/recruiter/applications?page=0&size=10"
# Expected: Paginated list of applications
```

**Test 2.3: List Vacancies**
```bash
curl -u recruiter:admin123 "http://localhost:8080/api/recruiter/vacancies?page=0&size=10"
# Expected: Paginated list of vacancies
```

**Test 2.4: Create Application** (requires valid vacancy_id and candidate data)
```bash
curl -X POST -u recruiter:admin123 \
  -H "Content-Type: application/json" \
  -d '{"candidateInfo":{"firstName":"Test","lastName":"User","email":"test@example.com","phone":"+79001234567"},"vacancyId":1,"comment":"Test application"}' \
  http://localhost:8080/api/recruiter/applications
# Expected: Created application object with ID
```

#### Part 3: HM (Hiring Manager) Endpoints

**Test 3.1: Pending Applications**
```bash
curl -u hm:admin123 "http://localhost:8080/api/hm/pending?page=0&size=10"
# Expected: Paginated list of applications pending HM review
```

**Test 3.2: Get Application Details**
```bash
curl -u hm:admin123 http://localhost:8080/api/hm/applications/{id}
# Replace {id} with actual application ID
# Expected: Detailed application information
```

**Test 3.3: Make Decision** (requires valid application ID)
```bash
curl -X POST -u hm:admin123 \
  -H "Content-Type: application/json" \
  -d '{"decision":"APPROVED","comment":"Great candidate","overallRating":5,"communicationSkills":5,"technicalKnowledge":5,"motivation":5,"teamFit":5}' \
  http://localhost:8080/api/hm/applications/{id}/decision
# Expected: Updated application with new status
```

#### Part 4: Admin Endpoints

**Test 4.1: List All Vacancies**
```bash
curl -u admin:admin123 "http://localhost:8080/api/admin/vacancies?page=0&size=10"
# Expected: Paginated list of all vacancies
```

**Test 4.2: List All Users**
```bash
curl -u admin:admin123 "http://localhost:8080/api/admin/users?page=0&size=10"
# Expected: Paginated list of all users
```

**Test 4.3: Create Vacancy**
```bash
curl -X POST -u admin:admin123 \
  -H "Content-Type: application/json" \
  -d '{"code":"INT-2025-TEST","title":"Test Internship","description":"Test description","department":"IT","location":"Moscow","positionsAvailable":1,"startDate":"2025-07-01","endDate":"2025-12-31","active":true,"hiringManagerId":3}' \
  http://localhost:8080/api/admin/vacancies
# Expected: Created vacancy object with ID
```

#### Part 5: Public Endpoints

**Test 5.1: Candidate Status** (public - no auth required)
```bash
curl "http://localhost:8080/api/candidate/status?token=test-token-1"
# Expected: Array of applications for that candidate
```

#### Part 6: Import/Export Endpoints

**Test 6.1: Import Applications** (requires valid Excel file)
```bash
curl -X POST -u recruiter:admin123 \
  -F "file=@sample_candidates.xlsx" \
  -F "vacancyId=1" \
  http://localhost:8080/api/import-export/import
# Expected: Import batch summary with batch ID
```

**Test 6.2: Check Import Status**
```bash
curl -u recruiter:admin123 http://localhost:8080/api/import-export/imports/{batchId}
# Expected: Import status and statistics
```

**Test 6.3: Export Approved Candidates**
```bash
curl -u recruiter:admin123 \
  "http://localhost:8080/api/import-export/export/approved" \
  -o approved_candidates.csv
# Expected: CSV file download
```

## Test Users

The system is seeded with the following test users:

| Username  | Password  | Role      | Description           |
|-----------|-----------|-----------|----------------------|
| admin     | admin123  | ADMIN     | System administrator |
| recruiter | admin123* | RECRUITER | Recruiter user       |
| hm        | admin123* | HM        | Hiring manager       |

*Note: Currently all test users use the same password hash `admin123` due to seed data configuration. This should be fixed in production.

## Test Candidates

The system is seeded with 3 test candidates:

1. Ivan Petrov - token: `test-token-1`
2. Maria Sidorova - token: `test-token-2`
3. Alexey Ivanov - token: `test-token-3`

## Expected Results

### Success Indicators

✓ All infrastructure endpoints return 200 OK  
✓ Health check returns `{"status":"UP"}`  
✓ OpenAPI docs return valid JSON  
✓ Authenticated endpoints accept valid credentials  
✓ RBAC is enforced (users can only access their role's endpoints)  
✓ Public endpoints work without authentication  
✓ Database queries return expected data structures  

### Common Issues

**401 Unauthorized**
- Check username/password combination
- Verify user exists in database
- Ensure credentials are sent with `-u username:password`

**403 Forbidden**
- User doesn't have required role for this endpoint
- Check RBAC configuration

**500 Internal Server Error**
- Check backend logs for stack trace
- Verify database connection
- Ensure migrations have run successfully

**Empty Responses**
- Database might be empty (no data created yet)
- This is normal if no applications have been created
- Try creating some data first

## Troubleshooting

### Backend Not Starting

```bash
# Check if port 8080 is already in use
lsof -i :8080

# Check backend logs
tail -f /tmp/backend.log  # or wherever logs are configured

# Verify database is running
docker ps | grep postgres
```

### Database Connection Issues

```bash
# Check PostgreSQL is running
docker exec x5-recruitment-db pg_isready -U recruitment

# Connect to database manually
docker exec -it x5-recruitment-db psql -U recruitment -d recruitment

# Check migrations
SELECT * FROM flyway_schema_history;
```

### Authentication Issues

```bash
# Verify users exist
docker exec -it x5-recruitment-db psql -U recruitment -d recruitment \
  -c "SELECT username, email, active FROM users;"

# Check user roles
docker exec -it x5-recruitment-db psql -U recruitment -d recruitment \
  -c "SELECT u.username, ur.role FROM users u JOIN user_roles ur ON u.id = ur.user_id;"
```

## Automation

For CI/CD pipelines, the smoke test script returns:
- Exit code 0: All tests passed
- Exit code 1: At least one test failed

Example Jenkins/GitHub Actions usage:
```bash
./docs/smoke.sh || exit 1
```

## Next Steps

After smoke tests pass:
1. Run full integration test suite (if available)
2. Perform load testing
3. Security scan
4. Manual QA testing
5. Deploy to staging environment
