# Testing Guide: Status Change and Stager Account Creation

## Prerequisites
1. Database running (PostgreSQL)
2. Backend running on http://localhost:8080
3. Frontend running on http://localhost:3000
4. Admin/Recruiter account for testing

## Part 1: Test Status Change in Candidate Card

### Setup
1. Login as recruiter/admin
2. Navigate to Applications list
3. Select any application to view details

### Test Steps
1. On the application detail page, click "Изменить статус" (Change Status) button
2. Select a new status from the dropdown (e.g., "Скрининг", "Интервью назначено")
3. Add an optional comment
4. Click "Сохранить" (Save)

### Expected Results
- ✅ Status change dialog closes
- ✅ Success notification appears
- ✅ Application status badge updates immediately on the detail page
- ✅ Status change appears in status history section
- ✅ Navigate back to applications list - status is updated there too
- ✅ If candidate has a stager account - status is visible in their dashboard

### Debugging Failed Tests
If status change fails:
1. Check browser console for errors
2. Check network tab - look for PATCH request to `/api/recruiter/applications/{id}/status`
3. Verify request body has `newStatus` field (not `status`)
4. Check backend logs for errors

## Part 2: Test Stager Account Creation During Excel Import

### Prepare Test Excel File
Create an Excel file with these columns (or use existing sample):
```
Фамилия | Имя | ТГ | Телефон | Почта | Резюме | Первый приоритет | Второй приоритет | Курс | Специальность | Другая специальность | График | Город | Другой город | Откуда узнал | Год рождения | Гражданство | ВУЗ | Другой ВУЗ | Языки | Дата заявки
```

Example row:
```
Иванов | Иван | @ivanov | +79001234567 | ivan.test@example.com | link | Программа А | Программа Б | 3 | Информатика | | Полный день | Москва | | Интернет | 2000 | РФ | МГУ | | Английский;Русский | 2024-01-15
```

### Test Steps

#### Test Case 1: Import New Candidate
1. Login as recruiter/admin
2. Navigate to Import/Export page
3. Upload the Excel file
4. Wait for import to complete

**Expected Results:**
- ✅ Import succeeds with summary showing:
  - Total rows processed
  - Success rows
  - Failed rows (should be 0 or minimal)
  - **Users created** (should show 1 for new candidate)
  - **Users linked** (should show 0)

5. Open import batch details to verify the stats

#### Test Case 2: Login as Stager
1. Logout from recruiter account
2. Go to login page
3. Enter credentials:
   - **Username**: `ivan.test` (or generated from email prefix)
   - **Password**: `Stager2024!` (or value from STAGER_DEFAULT_PASSWORD env var)
4. Login

**Expected Results:**
- ✅ Login succeeds
- ✅ Redirected to stager dashboard
- ✅ Can see own application(s)
- ✅ Application status is displayed correctly

#### Test Case 3: Verify Stager Sees Status Updates
1. Keep stager logged in
2. In another browser/incognito window, login as recruiter
3. Change the application status for the stager's application
4. Go back to stager window and refresh

**Expected Results:**
- ✅ Updated status is visible in stager's view
- ✅ Status history shows the change

#### Test Case 4: Re-import Same Candidate
1. Login as recruiter
2. Import the same Excel file again
3. Check import results

**Expected Results:**
- ✅ Import succeeds
- ✅ **Users created**: 0 (no new users)
- ✅ **Users linked**: 1 (existing user linked)
- ✅ No duplicate user accounts created
- ✅ Existing application is reused or updated

#### Test Case 5: Import Candidate with Existing Email
1. Create a user manually with email `test.user@example.com` and role RECRUITER
2. Import Excel with candidate having same email
3. Check import results

**Expected Results:**
- ✅ Import succeeds
- ✅ **Users linked**: 1
- ✅ Existing user gets STAGER role added
- ✅ User can still login and see their applications

### Debugging Failed Tests

#### Issue: User account not created
1. Check backend logs for errors during import
2. Verify candidate email is valid
3. Check database: `SELECT * FROM users WHERE email = 'ivan.test@example.com'`

#### Issue: Cannot login as stager
1. Verify username generation:
   - Username = email prefix (before @)
   - Check: `SELECT username, email FROM users WHERE email = 'ivan.test@example.com'`
2. Verify password (check env var STAGER_DEFAULT_PASSWORD or use default)
3. Check user has STAGER or CANDIDATE role:
   ```sql
   SELECT u.username, ur.role 
   FROM users u 
   JOIN user_roles ur ON u.id = ur.user_id 
   WHERE u.email = 'ivan.test@example.com'
   ```

#### Issue: Stager cannot see applications
1. Verify candidate exists with same email:
   ```sql
   SELECT * FROM candidates WHERE email = 'ivan.test@example.com'
   ```
2. Verify applications exist:
   ```sql
   SELECT * FROM applications WHERE candidate_id = (
     SELECT id FROM candidates WHERE email = 'ivan.test@example.com'
   )
   ```
3. Check backend logs when stager tries to access `/api/stager/application`

## Database Verification

### Check Import Statistics
```sql
SELECT 
  id,
  file_name,
  total_rows,
  success_rows,
  failed_rows,
  users_created,
  users_linked,
  uploaded_at
FROM import_batches
ORDER BY uploaded_at DESC
LIMIT 5;
```

### Check User Creation
```sql
-- Find users created today
SELECT 
  u.id,
  u.username,
  u.email,
  u.first_name,
  u.last_name,
  u.status,
  ur.role
FROM users u
LEFT JOIN user_roles ur ON u.id = ur.user_id
WHERE u.created_at >= CURRENT_DATE
ORDER BY u.created_at DESC;
```

### Check Candidate-User Linkage
```sql
-- Find candidates and their linked users (via email)
SELECT 
  c.id as candidate_id,
  c.email,
  c.first_name || ' ' || c.last_name as candidate_name,
  u.id as user_id,
  u.username,
  u.status
FROM candidates c
LEFT JOIN users u ON LOWER(u.email) = LOWER(c.email)
ORDER BY c.created_at DESC
LIMIT 10;
```

## Known Limitations / Future Enhancements

1. **Password Management**: Currently uses fixed/env-based password. Future: implement email-based password reset
2. **Username Collisions**: System adds numeric suffix. Future: allow custom usernames
3. **Bulk Import Performance**: Large files may take time. Future: async processing with progress updates
4. **Email Validation**: Basic validation only. Future: send verification emails

## Environment Variables

Set these in production:
```bash
# Custom default password for imported stagers (development/testing only)
export STAGER_DEFAULT_PASSWORD="YourSecurePasswordHere"
```

For production, implement proper password management system.
