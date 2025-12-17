# Testing Guide: HM Functionality Removal & Fixes

This guide provides step-by-step instructions to verify that the HM functionality has been completely removed and all related fixes are working correctly.

## Prerequisites

- Docker and Docker Compose installed
- OR: Java 21, Node.js 18+, PostgreSQL 17
- Access to the repository

## Quick Start with Docker

```bash
# Clone and start all services
git clone https://github.com/Pe4EHKA/X5GroupRecruitment.git
cd X5GroupRecruitment
docker compose up --build
```

**Services will be available at:**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

## Test Scenarios

### 1. ✅ Verify HM Functionality is Removed

#### Backend API Tests

1. **Check Swagger UI** (http://localhost:8080/swagger-ui.html)
   - ✅ Should NOT see "Hiring Manager API" section
   - ✅ Should NOT see `/api/hm/*` endpoints
   - ✅ Should NOT see `/api/recruiter/applications/{id}/send-to-hm` endpoint

2. **Test HM Endpoints Return 404**
   ```bash
   # Should return 404
   curl -X GET http://localhost:8080/api/hm/pending \
     -u recruiter:recruiter123
   
   # Should return 404
   curl -X POST http://localhost:8080/api/recruiter/applications/1/send-to-hm \
     -u recruiter:recruiter123 \
     -H "Content-Type: application/json"
   ```

3. **Check Dashboard Metrics**
   ```bash
   # Should NOT include hmReviewCount
   curl -X GET http://localhost:8080/api/recruiter/dashboard/metrics \
     -u recruiter:recruiter123
   
   # Expected response:
   # {
   #   "newCount": X,
   #   "screeningCount": Y,
   #   "interviewCount": Z,
   #   "approvedCount": A,
   #   "rejectedCount": B,
   #   "slaBreachCount": C,
   #   "totalCount": D
   # }
   # Note: NO hmReviewCount field
   ```

#### Frontend UI Tests

1. **Login as Recruiter**
   - Navigate to http://localhost:3000/login
   - Username: `recruiter`
   - Password: `recruiter123`

2. **Dashboard Check**
   - ✅ Should NOT see "На рассмотрении HM" metric card
   - ✅ Should see: Новые, Скрининг, На интервью, Одобрено, Отклонено, Всего

3. **Applications List** (http://localhost:3000/recruiter/applications)
   - ✅ Status filter should NOT have "Ожидает HM" option
   - ✅ Should have: Все, Новые, Скрининг, Интервью, Одобрено, Отклонено

4. **Application Detail Page**
   - Click on any application
   - ✅ Should NOT see "Отправить на HM" button
   - ✅ Should only see "Изменить статус" button
   - Click "Изменить статус"
   - ✅ Status dropdown should NOT have "Отправить на HM" option
   - ✅ Should have: Скрининг, Интервью назначено, Одобрено, Отклонено

5. **Navigation Check**
   - ✅ Should NOT see any HM-related menu items
   - ✅ /hm routes should return 404

6. **Login as HM (should fail)**
   - Try to login with HM credentials
   - Username: `hm`
   - Password: `hm123`
   - ✅ Should still be able to login (user exists)
   - ✅ But should not have access to /hm routes (will show 404 or access denied)

### 2. ✅ Verify 400 Status Error is Fixed

#### Test Invalid Token

1. **Access status page with invalid token**
   ```bash
   # Open in browser (check console for errors)
   http://localhost:3000/status/invalid-token
   ```
   
   **Expected behavior:**
   - ✅ Page should show "Заявки не найдены" message
   - ✅ Browser console should NOT show 400 errors
   - ✅ Network tab should NOT show failed requests to `/api/candidate/status`

2. **Access status page without token**
   ```bash
   # This will be handled by Next.js routing
   http://localhost:3000/status/
   ```
   
   **Expected behavior:**
   - ✅ Should show 404 page (Next.js routing)
   - ✅ No API calls should be made

3. **Test with valid token**
   - First, get a valid token from the database or create a candidate
   ```bash
   # Get candidates and their tokens
   curl -X GET http://localhost:8080/api/admin/... \
     -u admin:admin123
   ```
   - Access: http://localhost:3000/status/{valid-token}
   - ✅ Should display application status correctly
   - ✅ Should show status stepper with 4 steps (not 5)

#### Backend API Test

```bash
# Test without token parameter (should return 400)
curl -X GET http://localhost:8080/api/candidate/status

# Test with empty token (should return 400)
curl -X GET "http://localhost:8080/api/candidate/status?token="

# Test with valid token (should return 200)
curl -X GET "http://localhost:8080/api/candidate/status?token=SOME_VALID_TOKEN"
```

### 3. ✅ Verify Auto-refresh After Import

#### Preparation

1. **Login as Recruiter**
   - Navigate to http://localhost:3000/login
   - Username: `recruiter`
   - Password: `recruiter123`

2. **Prepare Test Excel File**
   
   Create a file `test_applications.xlsx` with columns:
   ```
   ФИО | Email | Телефон | Университет | Курс | Вакансия
   Иванов Иван Иванович | ivanov@test.ru | +79991234567 | МГУ | 3 | Backend Developer
   Петров Петр Петрович | petrov@test.ru | +79991234568 | МФТИ | 2 | Frontend Developer
   ```

#### Test Steps

1. **Navigate to Import Page**
   - Go to http://localhost:3000/recruiter/import
   
2. **Note Current Application Count**
   - Go to http://localhost:3000/recruiter/applications
   - Remember the total number of applications

3. **Import Excel File**
   - Go back to http://localhost:3000/recruiter/import
   - Click "Выбрать файл" and select your Excel file
   - Click "Загрузить"
   
4. **Verify Auto-refresh**
   - ✅ Should see import progress indicator
   - ✅ Should see success message after import
   - ✅ Import result should show:
     ```
     Обработано строк: 2
     Успешно: 2
     Ошибок: 0
     ```
   
5. **Check Applications List Updates Automatically**
   - Navigate to http://localhost:3000/recruiter/applications
   - ✅ Should see new applications WITHOUT refreshing the page
   - ✅ Application count should increase by the number of imported applications
   - ✅ New applications should appear in the table

6. **Verify Dashboard Updates**
   - Navigate to http://localhost:3000/recruiter/dashboard
   - ✅ Metrics should reflect the new applications
   - ✅ "Новые" count should have increased

### 4. ✅ Application Status Flow

Verify the complete status workflow still works without HM:

1. **Create New Application** (via import or API)

2. **Status Changes:**
   ```
   NEW → SCREENING → INTERVIEW_SCHEDULED → INTERVIEW_COMPLETED → APPROVED
                                                                  → REJECTED
   ```

3. **Test Status Changes:**
   - Open any application detail
   - Change status from NEW to SCREENING ✅
   - Change status from SCREENING to INTERVIEW_SCHEDULED ✅
   - Change status from INTERVIEW_SCHEDULED to APPROVED ✅
   - Change status from SCREENING to REJECTED ✅

4. **Verify Status Display:**
   - Check status badge colors are correct
   - Check status history is recorded
   - Check candidate status page shows correct step in stepper (4 steps total)

### 5. ✅ Status Page Stepper

1. **Access Candidate Status Page**
   - Get a valid status token from database
   - Navigate to http://localhost:3000/status/{token}

2. **Verify Stepper:**
   - ✅ Should show 4 steps (not 5):
     1. Новая заявка
     2. Скрининг
     3. Интервью
     4. Финал
   - ✅ Should NOT show "Рассмотрение" step
   - ✅ Active step should match application status

## Build Verification

### Backend Build

```bash
cd apps/backend
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
mvn clean compile
# Should succeed with no errors

mvn test
# All tests should pass (15/15)
```

### Frontend Build

```bash
cd apps/frontend
npm install
npx next build
# Should build successfully
# Should NOT have TypeScript errors
```

## Database Verification (Optional)

### Check for Applications in Old Status

```sql
-- Connect to PostgreSQL
docker exec -it x5grouprecruitment-db-1 psql -U recruitment

-- Check for applications in PENDING_HM_REVIEW status
SELECT id, status, candidate_id, vacancy_id, created_at 
FROM applications 
WHERE status = 'PENDING_HM_REVIEW';

-- If any exist, optionally update them:
UPDATE applications 
SET status = 'SCREENING' 
WHERE status = 'PENDING_HM_REVIEW';
```

## Checklist Summary

Use this checklist for smoke testing:

- [ ] **HM Removal**
  - [ ] No HM endpoints in Swagger UI
  - [ ] No "Send to HM" buttons in UI
  - [ ] No HM metric cards in dashboard
  - [ ] No HM statuses in dropdowns
  - [ ] /hm routes return 404

- [ ] **400 Error Fix**
  - [ ] Invalid token doesn't cause 400 errors
  - [ ] Status page handles errors gracefully
  - [ ] Empty token returns proper 400

- [ ] **Auto-refresh**
  - [ ] Import shows success message
  - [ ] Applications list updates automatically
  - [ ] Dashboard metrics update automatically
  - [ ] No manual page refresh needed

- [ ] **Status Flow**
  - [ ] Status changes work correctly
  - [ ] Status stepper has 4 steps
  - [ ] Status badges display correctly
  - [ ] Status history is recorded

- [ ] **Build**
  - [ ] Backend compiles successfully
  - [ ] Backend tests pass (15/15)
  - [ ] Frontend builds successfully
  - [ ] No TypeScript errors

## Expected Results

After completing all tests:

✅ **All HM functionality removed**
- No HM endpoints
- No HM UI elements
- No HM status values

✅ **No 400 errors**
- Status page handles invalid tokens
- Proper error messages shown
- No console errors

✅ **Auto-refresh works**
- Import updates lists automatically
- No manual refresh needed
- Success feedback shown

✅ **Application flow works**
- Status changes successful
- Workflow continues without HM step
- All user roles functional

## Troubleshooting

### Issue: Cannot access endpoints

**Solution:** Ensure services are running:
```bash
docker compose ps
# All services should be "Up"
```

### Issue: Database connection errors

**Solution:** Wait for database to initialize:
```bash
docker compose logs db | grep "ready to accept connections"
```

### Issue: Frontend build fails

**Solution:** Clear cache and reinstall:
```bash
rm -rf node_modules apps/frontend/node_modules
rm -rf .next apps/frontend/.next
npm install
cd apps/frontend && npx next build
```

### Issue: 401 Unauthorized errors

**Solution:** Use correct credentials:
- Admin: `admin` / `admin123`
- Recruiter: `recruiter` / `recruiter123`
- Stager: `stager` / `stager123`

## Summary

This testing guide verifies:
1. ✅ Complete removal of HM functionality
2. ✅ 400 status errors are fixed
3. ✅ Auto-refresh works after import
4. ✅ Application workflow continues smoothly
5. ✅ All builds are successful

The system is now fully functional without any HM-related features.
