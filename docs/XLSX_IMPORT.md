# XLSX Import Feature Documentation

## Overview

The XLSX import feature allows recruiters to import candidate applications from Excel files with comprehensive validation, deduplication, and error tracking.

## Supported Excel Format

The system expects an Excel file (`.xlsx`) with the following structure:

### Required Sheet
- **Sheet Name**: "Лист1" (or first sheet if named differently)
- **Header Row**: Row 1 must contain column headers

### Expected Columns (21 total)

The import uses **header-based mapping**, meaning columns can be in any order as long as the headers match exactly:

| # | Header Name | Required | Description | Example |
|---|------------|----------|-------------|---------|
| 1 | Фамилия | ✓ | Last name | Иванов |
| 2 | Имя | ✓ | First name | Иван |
| 3 | ТГ | | Telegram handle | @ivanov или ivanov |
| 4 | Телефон | * | Phone number | 7(999)999-99-99 |
| 5 | Почта | * | Email address | ivan@example.com |
| 6 | Резюме | | Resume link/path | https://... |
| 7 | Первый приоритет | ✓ | First priority program | Backend Development |
| 8 | Второй приоритет | | Second priority program | Data Science |
| 9 | Курс | | Course/year of study | 3 |
| 10 | Специальность | | Major/speciality | Информатика |
| 11 | Другая специальность | | Other speciality | |
| 12 | График | | Schedule preference | Полный день |
| 13 | Город | | City | Москва |
| 14 | Другой город | | Other city | |
| 15 | Откуда узнал | | How learned about program | Университет |
| 16 | Год рождения | | Birth year (1900-current) | 2000 |
| 17 | Гражданство | | Citizenship | РФ |
| 18 | ВУЗ | | University name | МГУ |
| 19 | Другой ВУЗ | | Other university | |
| 20 | Языки | | Programming languages | Python; R; SQL |
| 21 | Дата заявки | ✓ | Application date | Excel datetime |

\* At least one of "Телефон" or "Почта" is required

## Validation Rules

### Required Fields
- **Фамилия** AND **Имя** (First and Last name)
- **Телефон** OR **Почта** (At least one contact method)
- **Первый приоритет** (First priority program)
- **Дата заявки** (Application submission date)

### Field Validation
- **Год рождения**: Must be between 1900 and current year
- **Телефон**: Will be normalized to E.164 format (+7XXXXXXXXXX)
- **Почта**: Will be normalized to lowercase
- **Языки**: Will be split by semicolon (;) and stored as array

## Data Normalization

The import process automatically normalizes data:

### Email
- Trimmed
- Converted to lowercase
- Example: `Ivan@Example.COM` → `ivan@example.com`

### Phone Number
- Removes all non-digit characters
- Converts to E.164 format for Russian numbers
- Stores both original and normalized versions
- Examples:
  - `7(999)999-99-99` → `+79999999999`
  - `8-999-999-99-99` → `+79999999999`
  - `+7 999 999 99 99` → `+79999999999`

### Telegram
- Removes leading @ if present
- Trimmed
- Example: `@ivanov` → `ivanov`

### Languages
- Split by semicolon (;)
- Each language trimmed
- Stored as JSON array
- Example: `Python; R; SQL` → `["Python", "R", "SQL"]`

## Deduplication Strategy

The system prevents duplicate candidates using a two-level strategy:

### Primary Key: Email
- If an email already exists in the database, the existing candidate is used
- Email comparison is case-insensitive (normalized to lowercase)

### Secondary Key: Phone Number (E.164)
- If no email match but phone number matches (after E.164 normalization)
- The existing candidate is used

### Merge Strategy for Existing Candidates
When a candidate is found by email or phone:
- **Keep existing data** in non-empty fields
- **Update empty fields** with new data
- Example: If existing candidate has no birth year, it will be updated from import

### Duplicate Applications
- One candidate can only have one application per vacancy
- If duplicate application is detected, the existing application is used (not recreated)

## Priority Handling

### Program Matching
The system tries to match priority values to existing vacancies:
1. **By Title** (case-insensitive exact match)
2. **By Code** (exact match)

### Unmapped Programs
If a program name is not found in the vacancy list:
- A placeholder vacancy is created with code `UNMAPPED_*`
- Status set to inactive
- Original value preserved in `raw_value`
- `is_mapped` flag set to `false` for tracking

This ensures no data is lost even if program names don't match exactly.

## Import Tracking

### Import Batch
Each import creates an `ImportBatch` record containing:
- File name
- Upload timestamp
- User who uploaded
- Total rows processed
- Successful imports
- Failed imports
- Completion status

### Error Reporting
Failed rows are recorded in `ImportRowError` with:
- Row number (from Excel)
- Error code (e.g., `VALIDATION_ERROR`, `PROCESSING_ERROR`)
- Detailed error message
- Snapshot of raw row data (JSONB)

## API Usage

### Import Applications
```http
POST /api/import-export/import
Content-Type: multipart/form-data
Authorization: Basic <credentials>

file: <excel-file.xlsx>
```

**Response:**
```json
{
  "batch": {
    "id": 1,
    "fileName": "applications_2024_01.xlsx",
    "uploadedById": 1,
    "uploadedByName": "recruiter",
    "uploadedAt": "2024-01-15T10:30:00",
    "totalRows": 100,
    "successRows": 95,
    "failedRows": 5,
    "completed": true
  },
  "errors": [
    {
      "rowNumber": 15,
      "errorCode": "VALIDATION_ERROR",
      "errorMessage": "Имя и Фамилия обязательны",
      "rawSnapshot": {
        "Фамилия": "",
        "Имя": "",
        "Почта": "test@example.com"
      }
    }
  ],
  "totalErrors": 5
}
```

### Get Import Batch Status
```http
GET /api/import-export/batches/{batchId}
Authorization: Basic <credentials>
```

### Get Import Batch Errors (Paginated)
```http
GET /api/import-export/batches/{batchId}/errors?page=0&size=20
Authorization: Basic <credentials>
```

## Error Codes

| Code | Description |
|------|-------------|
| `VALIDATION_ERROR` | Row failed validation (missing required fields, invalid data) |
| `PROCESSING_ERROR` | Error during row processing (database error, etc.) |
| `DUPLICATE_ERROR` | Duplicate application detected |

## Best Practices

### Preparing Excel Files
1. Ensure header row exactly matches expected column names
2. Use Excel date format for "Дата заявки" column
3. Provide at least email OR phone for each candidate
4. Check year of birth is in valid range
5. Separate multiple languages with semicolon (`;`)

### Handling Errors
1. Review import result summary
2. Check failed rows in error report
3. Fix data issues in Excel
4. Re-import file (duplicates will be merged, not recreated)

### Program Names
1. Ensure program names in "Первый приоритет" and "Второй приоритет" match existing vacancies
2. Create vacancies in system before import, or
3. Review unmapped entries after import and map them manually

## Notifications

After successful import, candidates receive email notifications:
- Subject: "Ваша заявка принята"
- Includes access token for status tracking
- Sent asynchronously via outbox pattern

## Performance Considerations

### Large Files
- The import uses Apache POI streaming for memory efficiency
- Can handle files with thousands of rows
- Processing is transactional (all or nothing per row)

### Recommended Limits
- Maximum file size: 10 MB (configured in `application.properties`)
- Recommended batch size: Up to 1000 rows per file
- For larger imports, split into multiple files

## Troubleshooting

### Common Issues

**Issue**: Import fails with "Missing required headers"
- **Solution**: Ensure all required column headers are present in row 1

**Issue**: Many validation errors for birth year
- **Solution**: Check that birth year is a number, not text

**Issue**: Phone numbers not being normalized
- **Solution**: Ensure phone numbers contain digits and country code (7 or 8 for Russia)

**Issue**: All programs showing as "UNMAPPED"
- **Solution**: Create matching vacancies in system first, or update program names in Excel

### Logging
- Import process logs at DEBUG level
- Check application logs for detailed error messages
- Log pattern: `com.x5.recruitment.application.service.XlsxImportService`

## Security

### Access Control
- Only users with RECRUITER or ADMIN roles can import
- Import is logged with user who performed the operation
- All imports are auditable via import_batches table

### Data Validation
- SQL injection prevented via JPA/Hibernate
- File type validation (only .xlsx and .xls accepted)
- Maximum file size enforced

## Database Schema

### Related Tables
- `import_batches` - Tracks each import operation
- `import_row_errors` - Stores errors for failed rows
- `candidates` - Enhanced with new fields
- `applications` - Includes submitted_at timestamp
- `application_preferences` - Stores priority rankings
- `vacancies` - Includes allow_unmapped flag

## Future Enhancements

Potential improvements for future versions:
- CSV format support
- Real-time import progress tracking
- Automatic program name fuzzy matching
- Bulk update of unmapped programs
- Excel template generator
- Import preview/dry-run mode
