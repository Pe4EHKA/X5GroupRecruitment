# Questionnaire System API Documentation

This document describes the API endpoints for the Internship Vacancy Questionnaire System.

## Overview

The questionnaire system allows HR to create customized questionnaires for internship vacancies with:
- **Mandatory questions**: Always shown to all applicants
- **Optional questions**: Randomly selected subset shown to each applicant
- **Multiple question types**: TEXT, SINGLE_CHOICE, MULTI_CHOICE, NUMBER, DATE, VIDEO
- **Validation rules**: Configurable per question
- **Video recording**: In-browser video recording with automatic transcription
- **Statistics**: Completion tracking and filtering for HR

## HR/Recruiter API Endpoints

### Question Management

#### Create Question for Vacancy
```http
POST /api/hr/vacancies/{vacancyId}/questions
Authorization: Basic {credentials}
Content-Type: application/json

{
  "text": "Why do you want to join our internship program?",
  "type": "TEXT",
  "mandatory": true,
  "randomPool": false,
  "orderIndex": 0,
  "validationRules": {
    "minLength": 50,
    "maxLength": 500
  },
  "weight": 2
}
```

**Response**: `VacancyQuestionResponse`

#### Update Question
```http
PUT /api/hr/vacancies/questions/{questionId}
Authorization: Basic {credentials}
Content-Type: application/json

{
  "text": "Updated question text",
  "type": "TEXT",
  "mandatory": true,
  "orderIndex": 1,
  "validationRules": {
    "minLength": 100,
    "maxLength": 1000
  }
}
```

#### Get All Questions for Vacancy
```http
GET /api/hr/vacancies/{vacancyId}/questions
Authorization: Basic {credentials}
```

**Response**: `List<VacancyQuestionResponse>`

#### Delete Question
```http
DELETE /api/hr/vacancies/questions/{questionId}
Authorization: Basic {credentials}
```

**Response**: `204 No Content`

#### Reorder Questions
```http
PUT /api/hr/vacancies/{vacancyId}/questions/reorder
Authorization: Basic {credentials}
Content-Type: application/json

[123, 456, 789]
```

### Statistics

#### Get Application Statistics
```http
GET /api/hr/applications/{applicationId}/statistics
Authorization: Basic {credentials}
```

**Response**: `ApplicationStatisticsResponse`
```json
{
  "applicationId": 1,
  "candidateId": 42,
  "candidateName": "Ivan Petrov",
  "candidateEmail": "ivan@example.com",
  "totalQuestions": 10,
  "answeredQuestions": 8,
  "mandatoryQuestions": 5,
  "mandatoryAnswered": 5,
  "optionalQuestions": 5,
  "optionalAnswered": 3,
  "completionPercentage": 80.0,
  "hasVideoAnswers": true,
  "videoQuestionsTotal": 2,
  "videoQuestionsAnswered": 1,
  "answers": [...]
}
```

#### Get Vacancy Statistics
```http
GET /api/hr/vacancies/{vacancyId}/statistics
Authorization: Basic {credentials}
```

**Response**: `List<ApplicationStatisticsResponse>`

## Intern/Stager API Endpoints

### Questionnaire

#### Get Questionnaire for Application
```http
GET /api/stager/application/{applicationId}/questionnaire
Authorization: Basic {credentials}
```

**Response**: `QuestionnaireResponse`
```json
{
  "applicationId": 1,
  "vacancyId": 10,
  "vacancyTitle": "Backend Developer Intern",
  "totalQuestions": 10,
  "mandatoryQuestions": 5,
  "optionalQuestions": 5,
  "questions": [
    {
      "questionId": 101,
      "originalQuestionId": 50,
      "text": "Why do you want to join our internship?",
      "type": "TEXT",
      "mandatory": true,
      "orderIndex": 0,
      "validationRules": {
        "minLength": 50,
        "maxLength": 500
      }
    },
    {
      "questionId": 102,
      "text": "Select your preferred technology stack",
      "type": "MULTI_CHOICE",
      "mandatory": false,
      "orderIndex": 1,
      "options": ["Java", "Python", "Go", "JavaScript"]
    }
  ]
}
```

### Answer Submission

#### Submit Answers (Batch)
```http
POST /api/stager/application/{applicationId}/answers
Authorization: Basic {credentials}
Content-Type: application/json

{
  "answers": [
    {
      "questionId": 101,
      "textValue": "I am passionate about backend development and want to learn from experienced developers..."
    },
    {
      "questionId": 102,
      "choiceValues": ["Java", "Python"]
    },
    {
      "questionId": 103,
      "numberValue": 3.5
    },
    {
      "questionId": 104,
      "dateValue": "2000-05-15"
    },
    {
      "questionId": 105,
      "mediaId": 789
    }
  ]
}
```

**Response**: `List<AnswerResponse>`

**Validation Error Response** (400):
```json
{
  "message": "Validation failed",
  "errors": [
    {
      "questionId": 101,
      "field": "textValue",
      "error": "Text must be at least 50 characters",
      "providedValue": "Too short"
    },
    {
      "questionId": 102,
      "field": "choiceValues",
      "error": "Invalid choice: C++",
      "providedValue": "C++"
    }
  ]
}
```

#### Get My Answers
```http
GET /api/stager/application/{applicationId}/answers
Authorization: Basic {credentials}
```

**Response**: `List<AnswerResponse>`

## Media API Endpoints

### Video Upload

#### Upload Video
```http
POST /api/media/upload
Authorization: Basic {credentials}
Content-Type: multipart/form-data

file: <video file>
```

**Response**: `VideoUploadResponse`
```json
{
  "mediaId": 789,
  "storageKey": "videos/abc123.webm",
  "mimeType": "video/webm",
  "fileSize": 5242880,
  "message": "Video uploaded successfully",
  "transcriptionInitiated": true
}
```

#### Get Media Metadata
```http
GET /api/media/{mediaId}
Authorization: Basic {credentials}
```

**Response**: `MediaResponse`
```json
{
  "id": 789,
  "storageKey": "videos/abc123.webm",
  "mimeType": "video/webm",
  "fileSize": 5242880,
  "duration": 120,
  "streamUrl": "/api/media/789/stream",
  "createdAt": "2025-12-17T10:00:00",
  "transcription": {
    "id": 100,
    "status": "DONE",
    "text": "Transcribed text of the video...",
    "language": "ru",
    "attempts": 1,
    "createdAt": "2025-12-17T10:00:00",
    "updatedAt": "2025-12-17T10:02:00"
  }
}
```

#### Stream Video
```http
GET /api/media/{mediaId}/stream
Authorization: Basic {credentials}
```

**Response**: Video stream (Content-Type: video/webm or video/mp4)

## Question Types and Validation

### TEXT
**Validation Rules**:
- `minLength`: Minimum text length
- `maxLength`: Maximum text length
- `pattern`: Regex pattern

**Example**:
```json
{
  "type": "TEXT",
  "validationRules": {
    "minLength": 50,
    "maxLength": 1000,
    "pattern": "^[a-zA-Z\\s]+$"
  }
}
```

### NUMBER
**Validation Rules**:
- `min`: Minimum value
- `max`: Maximum value

**Example**:
```json
{
  "type": "NUMBER",
  "validationRules": {
    "min": 0,
    "max": 100
  }
}
```

### DATE
**Validation Rules**:
- `minDate`: Minimum date (ISO format)
- `maxDate`: Maximum date (ISO format)

**Example**:
```json
{
  "type": "DATE",
  "validationRules": {
    "minDate": "1990-01-01",
    "maxDate": "2010-12-31"
  }
}
```

### SINGLE_CHOICE
**Options**: List of allowed choices
**Answer**: Single selection from options

**Example**:
```json
{
  "type": "SINGLE_CHOICE",
  "options": ["Java", "Python", "Go", "JavaScript"]
}
```

### MULTI_CHOICE
**Options**: List of allowed choices
**Answer**: Multiple selections from options

**Example**:
```json
{
  "type": "MULTI_CHOICE",
  "options": ["Backend", "Frontend", "DevOps", "Data Science"]
}
```

### VIDEO
**Answer**: Media ID from uploaded video
**Transcription**: Automatically initiated on upload

**Example**:
```json
{
  "type": "VIDEO",
  "validationRules": {
    "maxDuration": 300
  }
}
```

## Configuration

Add to `application.properties`:

```properties
# Media storage
app.media.storage-path=./media-storage
app.media.max-video-size=104857600
app.media.max-video-duration=300

# File upload
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
```

## Security

- All endpoints require authentication (Basic Auth)
- HR endpoints: `RECRUITER` or `ADMIN` role
- Stager endpoints: `STAGER`, `CANDIDATE`, or `ADMIN` role
- Media endpoints: Any authenticated user
- Interns can only access their own applications

## Workflow

1. **HR creates vacancy questions**
   - Define mandatory and optional questions
   - Set validation rules
   - Configure random selection count

2. **Intern applies to vacancy**
   - Questionnaire auto-generated with mandatory + random optional questions
   - Questions fixed for this application

3. **Intern answers questions**
   - Submit answers (can update until final submission)
   - Upload video for VIDEO questions
   - Client-side validation before submission

4. **System processes video**
   - Video transcribed asynchronously
   - Status updates: PENDING → PROCESSING → DONE/FAILED

5. **HR reviews applications**
   - View statistics and completion rate
   - Filter by completion, video presence, etc.
   - Watch videos with transcriptions

## Testing

```bash
# Test with recruiter account
curl -u recruiter:recruiter123 \
  http://localhost:8080/api/hr/vacancies/1/questions

# Test with stager account
curl -u stager:stager123 \
  http://localhost:8080/api/stager/application/1/questionnaire

# Upload video
curl -u stager:stager123 \
  -F "file=@video.webm" \
  http://localhost:8080/api/media/upload
```
