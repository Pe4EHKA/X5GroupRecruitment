# Questionnaire System Feature

## Overview

The Questionnaire System extends the recruitment platform with customizable questionnaires for internship vacancies. This feature enables HR to create detailed assessments with various question types, while ensuring each applicant receives a consistent experience with mandatory questions plus a random selection of optional questions.

## Key Features

### For HR/Recruiters

1. **Flexible Question Builder**
   - Multiple question types: TEXT, NUMBER, DATE, SINGLE_CHOICE, MULTI_CHOICE, VIDEO
   - Mandatory vs. optional questions
   - Custom validation rules per question
   - Drag-and-drop reordering
   - Question weighting for future scoring

2. **Smart Random Selection**
   - Configure how many optional questions to show per applicant
   - Questions are randomly selected but remain fixed for each application
   - Ensures fair and consistent candidate experience

3. **Comprehensive Statistics**
   - Track completion rates per application
   - View mandatory vs. optional question answers
   - Filter candidates by video presence and completion percentage
   - Export statistics for analysis

4. **Video Interview Integration**
   - Watch candidate video responses
   - Automatic transcription of all video content
   - Side-by-side video player and transcript
   - Retry transcription if needed

### For Interns/Candidates

1. **Personalized Questionnaire**
   - Receive all mandatory questions + random optional questions
   - Questions remain stable (don't change on page refresh)
   - Clear indication of mandatory vs. optional

2. **Multi-format Answers**
   - Text responses with character limits
   - Number inputs with range validation
   - Date pickers
   - Single/multiple choice selections
   - In-browser video recording

3. **Video Recording**
   - Record directly in the browser (no external tools needed)
   - Preview before submission
   - Progress indicator during upload
   - Automatic transcription processing

4. **Validation & Feedback**
   - Real-time validation on client side
   - Clear error messages for invalid inputs
   - Cannot submit without answering mandatory questions

## Database Schema

```
vacancy_question
- Stores questions configured by HR for each vacancy
- Fields: text, type, mandatory, random_pool, order_index, validation_rules, options

application_question
- Snapshot of questions shown to each applicant
- Ensures stability even if vacancy questions change
- Fields: text, type, mandatory_snapshot, validation_rules_snapshot

application_answer
- Stores all types of answers
- Fields: text_value, number_value, date_value, choice_values, media_id

media
- Metadata for uploaded videos
- Fields: storage_key, mime_type, file_size, duration

transcription
- Video transcription data and status
- Fields: status (PENDING/PROCESSING/DONE/FAILED), text, language, error_message
```

## API Endpoints

See [QUESTIONNAIRE_API.md](./QUESTIONNAIRE_API.md) for complete API documentation.

### HR Endpoints
- `POST /api/hr/vacancies/{id}/questions` - Create question
- `PUT /api/hr/vacancies/questions/{id}` - Update question
- `GET /api/hr/vacancies/{id}/questions` - List questions
- `DELETE /api/hr/vacancies/questions/{id}` - Delete question
- `PUT /api/hr/vacancies/{id}/questions/reorder` - Reorder questions
- `GET /api/hr/applications/{id}/statistics` - Get application stats
- `GET /api/hr/vacancies/{id}/statistics` - Get vacancy stats

### Intern Endpoints
- `GET /api/stager/application/{id}/questionnaire` - Get questionnaire
- `POST /api/stager/application/{id}/answers` - Submit answers
- `GET /api/stager/application/{id}/answers` - Get submitted answers

### Media Endpoints
- `POST /api/media/upload` - Upload video
- `GET /api/media/{id}` - Get media metadata
- `GET /api/media/{id}/stream` - Stream video

## Architecture

### Question Selection Algorithm

1. When an application is created, the system:
   - Retrieves all mandatory questions for the vacancy
   - Retrieves all optional questions eligible for random selection
   - Randomly selects N optional questions (N = vacancy.optionalQuestionsToAsk)
   - Creates ApplicationQuestion records (snapshots)

2. Questions are fixed for the application lifecycle
   - Changes to vacancy questions don't affect existing applications
   - Ensures fairness and consistency

### Video Transcription Pipeline

1. **Upload**: Intern uploads video via `/api/media/upload`
2. **Storage**: Video saved to file system (configurable storage path)
3. **Transcription Record**: Created with status PENDING
4. **Background Worker**: Scheduled job runs every 60 seconds
5. **Processing**: 
   - Picks up PENDING transcriptions
   - Updates status to PROCESSING
   - Calls transcription service (mock in MVP)
   - Updates status to DONE or FAILED
6. **Retry**: Failed transcriptions retried up to 3 times

### Answer Validation

Validation occurs at multiple levels:
1. **Client-side**: Immediate feedback (to be implemented in frontend)
2. **Server-side**: Comprehensive validation before saving
3. **Type-specific**: Each question type has custom validation logic
4. **Rule-based**: Configurable rules (min/max length, pattern, range, etc.)

## Configuration

### Application Properties

```properties
# Media storage configuration
app.media.storage-path=./media-storage
app.media.max-video-size=104857600
app.media.max-video-duration=300

# File upload limits
spring.servlet.multipart.max-file-size=100MB
spring.servlet.multipart.max-request-size=100MB
```

### Security

- HR/Recruiter endpoints require `RECRUITER` or `ADMIN` role
- Intern endpoints require `STAGER`, `CANDIDATE`, or `ADMIN` role
- Media streaming requires authentication
- Interns can only access their own applications

## MVP Implementation

### Included
✅ Database schema and migrations
✅ Domain models and repositories
✅ Complete service layer
✅ RESTful API controllers
✅ Answer validation service
✅ File-based video storage
✅ Async transcription worker (mock)
✅ Statistics and filtering
✅ API documentation

### Not Included (Future Enhancements)
- Frontend UI components (documented API only)
- Real transcription API integration (Google Speech-to-Text, AWS Transcribe)
- S3/MinIO cloud storage
- Advanced analytics and reporting
- Question templates library
- Bulk question import/export

## Testing

### Manual API Testing

```bash
# 1. Create questions for a vacancy
curl -u recruiter:recruiter123 -X POST \
  http://localhost:8080/api/hr/vacancies/1/questions \
  -H "Content-Type: application/json" \
  -d '{
    "text": "Why do you want to join our program?",
    "type": "TEXT",
    "mandatory": true,
    "validationRules": {"minLength": 50, "maxLength": 500}
  }'

# 2. Get questionnaire as intern
curl -u stager:stager123 \
  http://localhost:8080/api/stager/application/1/questionnaire

# 3. Submit answers
curl -u stager:stager123 -X POST \
  http://localhost:8080/api/stager/application/1/answers \
  -H "Content-Type: application/json" \
  -d '{
    "answers": [
      {"questionId": 1, "textValue": "I am passionate about..."}
    ]
  }'

# 4. Upload video
curl -u stager:stager123 -X POST \
  http://localhost:8080/api/media/upload \
  -F "file=@video.webm"

# 5. View statistics
curl -u recruiter:recruiter123 \
  http://localhost:8080/api/hr/applications/1/statistics
```

### Integration Tests

See `apps/backend/src/test/java/com/x5/recruitment/` for test examples.

## Deployment

### Database Migration

Flyway migration `V11__add_questionnaire_system.sql` will run automatically on startup.

### Storage Directory

Ensure the media storage directory has proper permissions:
```bash
mkdir -p ./media-storage/videos
chmod 755 ./media-storage
```

### Docker

Update `docker-compose.yml` to mount media storage volume:
```yaml
services:
  backend:
    volumes:
      - ./media-storage:/app/media-storage
```

## Future Roadmap

1. **Phase 2**
   - Frontend UI components (React)
   - Real-time video recording widget
   - Interactive question builder
   - Statistics dashboard

2. **Phase 3**
   - Integration with transcription APIs
   - Cloud storage (S3/MinIO)
   - Advanced analytics
   - ML-based answer scoring

3. **Phase 4**
   - Question templates library
   - A/B testing for questions
   - Sentiment analysis on answers
   - Automated candidate ranking

## Support

For questions and issues, contact the development team or create an issue in the repository.
