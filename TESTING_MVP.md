# MVP System Testing Guide

## Overview
This guide provides instructions for testing the simplified MVP with 2 primary roles: HR and Stager.

## Prerequisites
- Java 21 installed
- Node.js 18+ installed
- Docker and Docker Compose (optional, for PostgreSQL)

## Starting the System

### Option 1: Using Docker (Recommended)
```bash
# From repository root
docker compose up --build
```

Services will be available at:
- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- PostgreSQL: localhost:5432

### Option 2: Local Development

#### 1. Start PostgreSQL
```bash
docker compose up postgres -d
```

#### 2. Start Backend
```bash
cd apps/backend
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
mvn spring-boot:run
```

Backend will be available at: http://localhost:8080

#### 3. Start Frontend
```bash
cd apps/frontend
npm install
npm run dev
```

Frontend will be available at: http://localhost:3000

## Test Credentials

| Username  | Password       | Role      | Redirect URL |
|-----------|---------------|-----------|--------------|
| recruiter | recruiter123  | RECRUITER | /hr          |
| stager    | stager123     | STAGER    | /stager      |
| admin     | admin123      | ADMIN     | /admin       |

## Test Scenarios

### 1. Login Flow (No Role Selector)

✅ **Test**: Login as HR
1. Navigate to http://localhost:3000
2. Click "HR (Recruiter)" quick login button OR enter `recruiter` / `recruiter123`
3. **Expected**: Automatic redirect to `/hr` dashboard
4. **Verify**: No role selection screen appears

✅ **Test**: Login as Stager
1. Navigate to http://localhost:3000
2. Click "Stager (Intern)" quick login button OR enter `stager` / `stager123`
3. **Expected**: Automatic redirect to `/stager` dashboard
4. **Verify**: No role selection screen appears

### 2. HR Dashboard (/hr)

✅ **Test**: View Applications List
1. Login as `recruiter:recruiter123`
2. Navigate to `/hr`
3. **Expected**: See table with applications
4. **Verify**: 
   - Columns: Candidate, Email, Phone, Vacancy, Status, Date
   - Pagination controls at bottom
   - Filter panel at top

✅ **Test**: Status Filter Chips
1. On `/hr` page, click "Новые" chip
2. **Expected**: Table filters to show only NEW applications
3. **Verify**: Chip appears filled/highlighted
4. Click chip again to deselect
5. **Expected**: Shows all applications again

✅ **Test**: Multiple Status Filters
1. Click "Новые" and "На скрининге" chips
2. **Expected**: Table shows applications with either status
3. **Verify**: Both chips are highlighted

✅ **Test**: Search Functionality
1. Type a candidate name, email, or phone in search box
2. **Expected**: Real-time filtering as you type
3. **Verify**: Only matching applications shown
4. Click X icon to clear search
5. **Expected**: All applications shown again

✅ **Test**: Clear All Filters
1. Apply some filters (status + search)
2. Click "Сбросить фильтры" button
3. **Expected**: All filters cleared, full list shown

✅ **Test**: Pagination
1. Change "Строк на странице" to 10
2. **Expected**: Only 10 rows shown
3. Click next page arrow
4. **Expected**: Next 10 applications shown
5. **Verify**: Page count and "X–Y из Z" text updates

### 3. Stager Dashboard (/stager)

✅ **Test**: View Own Application
1. Login as `stager:stager123`
2. Navigate to `/stager`
3. **Expected**: See "Моя заявка" page
4. **Verify**:
   - Application info card (vacancy, status, date)
   - Status history timeline with icons
   - Current status chip with color

✅ **Test**: Status History Timeline
1. On `/stager` page, check "История статусов" section
2. **Expected**: Timeline showing all status changes
3. **Verify**:
   - Dates and times for each status change
   - Status labels in Russian
   - Changed by (user name or "System")
   - Comments if any

✅ **Test**: No Applications
1. If stager has no applications:
2. **Expected**: Info alert "У вас пока нет заявок"
3. **Verify**: No errors, clean UI

### 4. API Endpoints (Manual Testing)

✅ **Test**: /api/auth/me
```bash
# HR user
curl -u recruiter:recruiter123 http://localhost:8080/api/auth/me

# Expected response:
{
  "id": 2,
  "username": "recruiter",
  "email": "recruiter@x5.ru",
  "displayName": "Анна Рекрутер",
  "roles": ["RECRUITER"]
}

# Stager user
curl -u stager:stager123 http://localhost:8080/api/auth/me

# Expected response:
{
  "id": 4,
  "username": "stager",
  "email": "stager@x5.ru",
  "displayName": "Иван Стажёр",
  "roles": ["STAGER"]
}
```

✅ **Test**: /api/hr/applications (with filters)
```bash
# All applications
curl -u recruiter:recruiter123 http://localhost:8080/api/hr/applications

# Filter by status
curl -u recruiter:recruiter123 "http://localhost:8080/api/hr/applications?statuses=NEW,SCREENING"

# Search
curl -u recruiter:recruiter123 "http://localhost:8080/api/hr/applications?search=иван"

# Pagination
curl -u recruiter:recruiter123 "http://localhost:8080/api/hr/applications?page=0&size=10"
```

✅ **Test**: /api/stager/application
```bash
# Get own applications
curl -u stager:stager123 http://localhost:8080/api/stager/application

# Expected: Array of applications with full details including statusHistory
```

✅ **Test**: /api/stager/profile
```bash
# Get profile
curl -u stager:stager123 http://localhost:8080/api/stager/profile

# Update profile
curl -u stager:stager123 -X PUT \
  -H "Content-Type: application/json" \
  -d '{"phone": "+79991234567", "city": "Москва"}' \
  http://localhost:8080/api/stager/profile
```

### 5. Security & Access Control

✅ **Test**: RBAC - HR cannot access Stager endpoints
```bash
curl -u recruiter:recruiter123 http://localhost:8080/api/stager/application
# Expected: 403 Forbidden
```

✅ **Test**: RBAC - Stager cannot access HR endpoints
```bash
curl -u stager:stager123 http://localhost:8080/api/hr/applications
# Expected: 403 Forbidden
```

✅ **Test**: Unauthorized access
```bash
curl http://localhost:8080/api/hr/applications
# Expected: 401 Unauthorized
```

✅ **Test**: /api/auth/me requires authentication
```bash
curl http://localhost:8080/api/auth/me
# Expected: 401 Unauthorized
```

### 6. Data Import (HR)

✅ **Test**: Import Excel File
1. Login as `recruiter:recruiter123`
2. Navigate to `/recruiter/import` (legacy route still works)
3. Upload sample Excel file
4. **Expected**: Success message with count of imported applications
5. Navigate to `/hr`
6. **Expected**: Imported applications appear in list
7. **Verify**: Can filter/search new applications

### 7. Export (HR)

✅ **Test**: Export Applications
1. Login as `recruiter:recruiter123`
2. Navigate to `/recruiter/export` (legacy route still works)
3. Click export button
4. **Expected**: CSV file downloads
5. **Verify**: File contains application data

## Common Issues & Troubleshooting

### Issue: 403 Forbidden on /api/auth/me
**Cause**: Wrong credentials or role
**Fix**: Double-check username/password

### Issue: Applications not loading in HR dashboard
**Cause**: Backend not running or CORS issue
**Fix**: 
1. Check backend is running on :8080
2. Check browser console for errors
3. Verify CORS config allows localhost:3000

### Issue: Stager sees "У вас пока нет заявок"
**Cause**: No applications linked to stager@x5.ru email
**Fix**: 
1. Import applications via HR
2. Or manually create application with candidate email = stager@x5.ru

### Issue: Filters not working
**Cause**: Backend query parameters not handled correctly
**Fix**: 
1. Check browser network tab for query params
2. Verify backend logs show correct filtering

## Success Criteria

All tests should pass with:
- ✅ No role selector on login page
- ✅ Auto-redirect based on user role
- ✅ HR can view, filter, and search applications
- ✅ Stager can view own applications with history
- ✅ RBAC enforced correctly (403 on unauthorized access)
- ✅ No security vulnerabilities (CodeQL passed)
- ✅ Clean UI with no console errors

## Notes

- Admin role still exists for backward compatibility but is not part of MVP UI flow
- HM role endpoints still exist but are not actively used in MVP
- Legacy `/recruiter/*` routes still work for import/export
- Database migration V9 adds stager test user automatically
