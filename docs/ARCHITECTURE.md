# X5 Recruitment System - Архитектура и дизайн

## A) System Context

### Акторы системы

1. **Recruiter (Рекрутер)** - управляет воронкой кандидатов, проводит скрининг
2. **Hiring Manager (HM)** - принимает финальные решения по кандидатам
3. **Admin** - администратор системы, полный доступ
4. **Candidate (Кандидат)** - проверяет статус своей заявки (read-only)

### Внешние системы

1. **Портал заявок** - источник заявок кандидатов
   - Интеграция: импорт через CSV/XLSX файлы (2 раза в неделю)
   - Направление: входящий

2. **Внутренняя ATS (Applicant Tracking System)**
   - Интеграция: экспорт одобренных кандидатов через CSV/API
   - Направление: исходящий
   - Допущение: ATS принимает CSV с определенным форматом

3. **Email-провайдер** (stub в MVP)
   - Интеграция: SMTP для отправки уведомлений
   - Направление: исходящий

### Границы системы

**Внутри системы:**
- Управление заявками и кандидатами
- Воронка отбора и статусы
- Структурированная обратная связь
- Уведомления кандидатам
- RBAC и безопасность
- Аудит действий

**Вне системы:**
- Сбор заявок (внешний портал)
- Финальный onboarding (внутренняя ATS)
- Календарное планирование интервью
- Видео-интервью
- Хранилище резюме (S3)

---

## B) Domain Model (DDD Light)

### Агрегаты и сущности

#### 1. Candidate (Агрегат)
**Назначение:** Представляет человека, подающего заявку

**Атрибуты:**
- id: Long
- firstName, lastName: String
- email: String (уникальный)
- phone: String
- resumePath: String
- additionalInfo: String
- accessToken: String (для self-service)

**Инварианты:**
- Email должен быть уникальным
- accessToken генерируется автоматически

**Связи:**
- 1-to-many с Application

#### 2. Application (Главный агрегат)
**Назначение:** Заявка кандидата на конкретную вакансию

**Атрибуты:**
- id: Long
- candidate: Candidate
- vacancy: Vacancy
- status: ApplicationStatus (enum)
- coverLetter: String
- notes: String
- assignedRecruiter: User
- screeningScore: Integer

**Инварианты:**
- Один кандидат может подать только одну заявку на одну вакансию
- Переходы между статусами должны быть валидными
- История статусов сохраняется автоматически

**Связи:**
- many-to-one с Candidate
- many-to-one с Vacancy
- many-to-one с User (recruiter)
- 1-to-many с StatusHistory
- 1-to-many с Interview
- 1-to-many с Feedback

#### 3. Vacancy
**Назначение:** Вакансия/программа стажировки

**Атрибуты:**
- id: Long
- code: String (уникальный)
- title: String
- description: String
- department, location: String
- positionsAvailable: Integer
- startDate, endDate: LocalDate
- active: Boolean
- hiringManager: User

**Инварианты:**
- Code должен быть уникальным
- Активные вакансии принимают заявки

#### 4. User
**Назначение:** Пользователь системы (recruiter, HM, admin)

**Атрибуты:**
- id: Long
- username, email: String (уникальные)
- passwordHash: String
- firstName, lastName: String
- roles: Set<UserRole>
- active: Boolean

**Инварианты:**
- Username и email уникальны
- Пароль хранится в виде BCrypt hash
- У пользователя должна быть хотя бы одна роль

#### 5. StatusHistory
**Назначение:** Аудит переходов между статусами

**Атрибуты:**
- id: Long
- application: Application
- fromStatus, toStatus: ApplicationStatus
- changedBy: User
- comment: String
- changedAt: LocalDateTime

#### 6. Interview
**Назначение:** Запланированное или проведенное интервью

**Атрибуты:**
- id: Long
- application: Application
- scheduledAt: LocalDateTime
- completedAt: LocalDateTime
- interviewer: User
- location: String
- notes: String
- completed: Boolean

#### 7. Feedback
**Назначение:** Структурированная обратная связь

**Атрибуты:**
- id: Long
- application: Application
- author: User
- rating: Integer (1-5)
- strengths, weaknesses: String
- recommendation: String
- generalComments: String
- shareWithCandidate: Boolean

#### 8. Notification (Outbox)
**Назначение:** Очередь уведомлений для кандидатов

**Атрибуты:**
- id: Long
- candidate: Candidate
- application: Application
- type: NotificationType
- subject, body: String
- sent: Boolean
- sentAt: LocalDateTime
- errorMessage: String
- attempts: Integer

**Инварианты:**
- Максимум 3 попытки отправки
- После успешной отправки sent = true

### Enums

**ApplicationStatus:**
- NEW - новая заявка
- SCREENING - на скрининге
- PENDING_HM_REVIEW - ожидает решения HM
- INTERVIEW_SCHEDULED - интервью назначено
- INTERVIEW_COMPLETED - интервью проведено
- APPROVED - одобрено
- REJECTED - отклонено
- WITHDRAWN - отозвано кандидатом
- ON_HOLD - в резерве

**UserRole:**
- ADMIN
- RECRUITER
- HM
- CANDIDATE

**NotificationType:**
- APPLICATION_RECEIVED
- STATUS_CHANGED
- INTERVIEW_SCHEDULED
- INTERVIEW_REMINDER
- APPROVED
- REJECTED
- GENERAL

---

## C) ERD и индексы PostgreSQL

### Таблицы

```sql
-- Users
users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(100) UNIQUE NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
)

user_roles (
  user_id BIGINT REFERENCES users(id),
  role VARCHAR(50) NOT NULL,
  PRIMARY KEY (user_id, role)
)

-- Candidates
candidates (
  id BIGSERIAL PRIMARY KEY,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  phone VARCHAR(20),
  resume_path VARCHAR(500),
  additional_info TEXT,
  access_token VARCHAR(100) UNIQUE NOT NULL,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
)

-- Vacancies
vacancies (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(50) UNIQUE NOT NULL,
  title VARCHAR(200) NOT NULL,
  description TEXT,
  department VARCHAR(100),
  location VARCHAR(100),
  positions_available INTEGER,
  start_date DATE,
  end_date DATE,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  hiring_manager_id BIGINT REFERENCES users(id),
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
)

-- Applications
applications (
  id BIGSERIAL PRIMARY KEY,
  candidate_id BIGINT NOT NULL REFERENCES candidates(id),
  vacancy_id BIGINT NOT NULL REFERENCES vacancies(id),
  status VARCHAR(50) NOT NULL DEFAULT 'NEW',
  cover_letter VARCHAR(500),
  notes TEXT,
  assigned_recruiter_id BIGINT REFERENCES users(id),
  screening_score INTEGER,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL,
  UNIQUE(candidate_id, vacancy_id)
)

-- Status History
status_history (
  id BIGSERIAL PRIMARY KEY,
  application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
  from_status VARCHAR(50),
  to_status VARCHAR(50) NOT NULL,
  changed_by_id BIGINT REFERENCES users(id),
  comment TEXT,
  changed_at TIMESTAMP NOT NULL
)

-- Interviews
interviews (
  id BIGSERIAL PRIMARY KEY,
  application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
  scheduled_at TIMESTAMP NOT NULL,
  completed_at TIMESTAMP,
  interviewer_id BIGINT REFERENCES users(id),
  location VARCHAR(100),
  notes TEXT,
  completed BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL,
  updated_at TIMESTAMP NOT NULL
)

-- Feedbacks
feedbacks (
  id BIGSERIAL PRIMARY KEY,
  application_id BIGINT NOT NULL REFERENCES applications(id) ON DELETE CASCADE,
  author_id BIGINT NOT NULL REFERENCES users(id),
  rating INTEGER,
  strengths TEXT,
  weaknesses TEXT,
  recommendation TEXT,
  general_comments TEXT,
  share_with_candidate BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL
)

-- Notifications (Outbox)
notifications (
  id BIGSERIAL PRIMARY KEY,
  candidate_id BIGINT NOT NULL REFERENCES candidates(id),
  application_id BIGINT REFERENCES applications(id),
  type VARCHAR(50) NOT NULL,
  subject VARCHAR(255) NOT NULL,
  body TEXT NOT NULL,
  sent BOOLEAN NOT NULL DEFAULT FALSE,
  sent_at TIMESTAMP,
  error_message TEXT,
  attempts INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMP NOT NULL
)
```

### Индексы

```sql
-- Users
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);

-- Candidates
CREATE INDEX idx_candidates_email ON candidates(email);
CREATE INDEX idx_candidates_phone ON candidates(phone);
CREATE INDEX idx_candidates_access_token ON candidates(access_token);

-- Vacancies
CREATE INDEX idx_vacancies_code ON vacancies(code);
CREATE INDEX idx_vacancies_status ON vacancies(active);

-- Applications (критичные для производительности)
CREATE INDEX idx_applications_status ON applications(status);
CREATE INDEX idx_applications_candidate ON applications(candidate_id);
CREATE INDEX idx_applications_vacancy ON applications(vacancy_id);
CREATE INDEX idx_applications_created ON applications(created_at);

-- Status History
CREATE INDEX idx_status_history_application ON status_history(application_id);
CREATE INDEX idx_status_history_changed_at ON status_history(changed_at);

-- Interviews
CREATE INDEX idx_interviews_application ON interviews(application_id);
CREATE INDEX idx_interviews_scheduled_at ON interviews(scheduled_at);

-- Feedbacks
CREATE INDEX idx_feedbacks_application ON feedbacks(application_id);
CREATE INDEX idx_feedbacks_created_at ON feedbacks(created_at);

-- Notifications (для outbox worker)
CREATE INDEX idx_notifications_candidate ON notifications(candidate_id);
CREATE INDEX idx_notifications_sent ON notifications(sent);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
```

---

## D) REST API спецификация

Полная спецификация доступна через Swagger UI: `/swagger-ui.html`

### Recruiter API

```
GET /api/recruiter/applications
  Query params: status, page, size, sort
  Response: Page<ApplicationDto>
  Auth: RECRUITER, ADMIN

GET /api/recruiter/applications/{id}
  Response: ApplicationDto
  Auth: RECRUITER, ADMIN

POST /api/recruiter/applications
  Body: CreateApplicationRequest
  Response: ApplicationDto (201 Created)
  Auth: RECRUITER, ADMIN

PATCH /api/recruiter/applications/{id}/status
  Body: ChangeStatusRequest
  Response: ApplicationDto
  Auth: RECRUITER, ADMIN

POST /api/recruiter/applications/{id}/send-to-hm
  Response: ApplicationDto
  Auth: RECRUITER, ADMIN
```

### HM API

```
GET /api/hm/pending
  Query params: page, size, sort
  Response: Page<ApplicationDto>
  Auth: HM, ADMIN

GET /api/hm/applications/{id}
  Response: ApplicationDto
  Auth: HM, ADMIN

POST /api/hm/applications/{id}/decision
  Body: HmDecisionRequest
  Response: ApplicationDto
  Auth: HM, ADMIN
```

### Candidate API

```
GET /api/candidate/status?token={token}
  Response: List<CandidateStatusDto>
  Auth: Public (token-based)
```

### Import/Export API

```
POST /api/import-export/import
  Content-Type: multipart/form-data
  Body: file (Excel/CSV)
  Response: List<ApplicationDto>
  Auth: RECRUITER, ADMIN

GET /api/import-export/export/approved
  Response: application/octet-stream (Excel file)
  Auth: RECRUITER, ADMIN
```

### DTOs

**CreateApplicationRequest:**
```json
{
  "firstName": "string",
  "lastName": "string",
  "email": "string",
  "phone": "string",
  "vacancyId": "number",
  "coverLetter": "string",
  "additionalInfo": "string"
}
```

**ChangeStatusRequest:**
```json
{
  "newStatus": "SCREENING|PENDING_HM_REVIEW|REJECTED|...",
  "comment": "string"
}
```

**HmDecisionRequest:**
```json
{
  "approved": "boolean",
  "rating": "number (1-5)",
  "strengths": "string",
  "weaknesses": "string",
  "recommendation": "string",
  "generalComments": "string",
  "shareWithCandidate": "boolean"
}
```

**ApplicationDto:**
```json
{
  "id": "number",
  "candidateId": "number",
  "candidateName": "string",
  "candidateEmail": "string",
  "vacancyId": "number",
  "vacancyTitle": "string",
  "status": "string",
  "coverLetter": "string",
  "notes": "string",
  "assignedRecruiterId": "number",
  "assignedRecruiterName": "string",
  "screeningScore": "number",
  "createdAt": "datetime",
  "updatedAt": "datetime"
}
```

**CandidateStatusDto:**
```json
{
  "candidateName": "string",
  "vacancyTitle": "string",
  "status": "string",
  "statusDescription": "string",
  "lastUpdated": "datetime",
  "appliedAt": "datetime"
}
```

### Статусы ответов

- 200 OK - успешно
- 201 Created - создано
- 400 Bad Request - ошибка валидации
- 401 Unauthorized - не авторизован
- 403 Forbidden - недостаточно прав
- 404 Not Found - не найдено
- 409 Conflict - конфликт (дубликат)
- 500 Internal Server Error - внутренняя ошибка

---

## E) Основные сценарии

### Сценарий 1: Импорт заявок

```
1. Recruiter загружает Excel файл через POST /api/import-export/import
2. ImportExportService.importFromExcel():
   - Парсит строки Excel
   - Для каждой строки:
     a. Проверяет существование кандидата по email
     b. Если нет - создает нового Candidate
     c. Если есть - использует существующего
     d. Проверяет дубликат заявки (candidate_id + vacancy_id)
     e. Создает Application со статусом NEW
     f. Сохраняет в БД (transaction)
3. ApplicationService.createApplication():
   - Создает Notification (APPLICATION_RECEIVED)
   - Сохраняет в outbox таблицу
4. NotificationWorker (async, каждые 30 сек):
   - Читает unsent notifications
   - Отправляет через EmailService
   - Помечает как sent
```

### Сценарий 2: Отправка на HM

```
1. Recruiter просматривает заявки: GET /api/recruiter/applications?status=SCREENING
2. Выбирает кандидата, отправляет на HM: POST /api/recruiter/applications/{id}/send-to-hm
3. ApplicationService.changeStatus():
   - Меняет статус на PENDING_HM_REVIEW
   - Создает StatusHistory (аудит)
   - Сохраняет Application (transaction)
   - Создает Notification (STATUS_CHANGED)
4. HM получает список: GET /api/hm/pending
5. HM просматривает детали: GET /api/hm/applications/{id}
6. HM принимает решение: POST /api/hm/applications/{id}/decision
7. HmService.makeDecision():
   - Создает Feedback с оценкой
   - Меняет статус на APPROVED/REJECTED
   - Создает StatusHistory
   - Сохраняет (transaction)
   - Создает Notification (APPROVED/REJECTED)
8. NotificationWorker отправляет уведомление кандидату
```

### Сценарий 3: Проверка статуса кандидатом

```
1. Кандидат получает email с access_token
2. Открывает ссылку: GET /api/candidate/status?token={token}
3. CandidateService.getCandidateStatus():
   - Находит Candidate по accessToken
   - Читает все Applications кандидата
   - Возвращает список CandidateStatusDto
4. Кандидат видит:
   - Вакансия
   - Текущий статус
   - Описание статуса
   - Дата подачи
   - Дата последнего обновления
```

---

## F) Архитектура приложения

### Модульный монолит

Структура пакетов (по слоям DDD):

```
com.x5.recruitment/
├── api/                        # API Layer
│   ├── controller/            # REST Controllers
│   │   ├── RecruiterController
│   │   ├── HmController
│   │   ├── CandidateController
│   │   └── ImportExportController
│   ├── dto/                   # Data Transfer Objects
│   │   ├── ApplicationDto
│   │   ├── CreateApplicationRequest
│   │   ├── ChangeStatusRequest
│   │   ├── HmDecisionRequest
│   │   └── CandidateStatusDto
│   └── exception/            # Exception Handling
│       └── GlobalExceptionHandler
│
├── application/              # Application Layer
│   └── service/             # Business Logic
│       ├── ApplicationService
│       ├── HmService
│       ├── CandidateService
│       └── ImportExportService
│
├── domain/                  # Domain Layer
│   ├── model/              # Entities & Value Objects
│   │   ├── User, UserRole
│   │   ├── Candidate
│   │   ├── Vacancy
│   │   ├── Application, ApplicationStatus
│   │   ├── StatusHistory
│   │   ├── Interview
│   │   ├── Feedback
│   │   └── Notification, NotificationType
│   └── repository/         # Repository Interfaces
│       ├── UserRepository
│       ├── CandidateRepository
│       ├── VacancyRepository
│       ├── ApplicationRepository
│       └── NotificationRepository
│
└── infrastructure/         # Infrastructure Layer
    ├── config/            # Configuration
    │   └── OpenApiConfig
    ├── security/          # Security
    │   ├── SecurityConfig
    │   └── CustomUserDetailsService
    └── notification/      # Notifications (Outbox)
        ├── NotificationWorker
        └── EmailService
```

### Транзакции

- `@Transactional` на сервисном слое
- Read-only транзакции для запросов
- Write транзакции для изменений
- Изоляция REPEATABLE_READ (PostgreSQL по умолчанию)

### Outbox Pattern

**Проблема:** Обеспечить надежную доставку уведомлений при изменении данных.

**Решение:**
1. В той же транзакции, где меняются данные, создается запись в `notifications`
2. Фоновый worker (`@Scheduled`) периодически проверяет unsent уведомления
3. Отправляет через EmailService
4. Помечает как sent или записывает ошибку
5. Retry до 3 раз

**Преимущества:**
- Атомарность (данные + уведомление в одной транзакции)
- Надежность (переотправка при ошибках)
- Не требует Kafka/RabbitMQ для MVP

### RBAC

Реализовано через Spring Security:

- `@PreAuthorize` на контроллерах/методах
- UserDetailsService загружает роли из БД
- Роли префиксуются `ROLE_` автоматически
- HTTP Basic Auth для MVP

**Правила доступа:**
- ADMIN: полный доступ ко всему
- RECRUITER: управление заявками, импорт/экспорт
- HM: просмотр pending заявок, принятие решений
- Candidate API: публичный (token-based)

### Аудит

1. **Автоматический:** Spring Data JPA Auditing
   - `@CreatedDate`, `@LastModifiedDate` на сущностях
   - Автоматическое заполнение при create/update

2. **Доменный:** StatusHistory
   - Все переходы между статусами записываются
   - Кто изменил, когда, комментарий

3. **Логирование:**
   - Все операции логируются на уровне DEBUG/INFO
   - Ошибки на уровне ERROR

---

## G) План реализации MVP

### Итерация 1: Core Foundation (неделя 1)

**Задачи:**
- [x] Настроить Maven проект с зависимостями
- [x] Создать доменную модель (entities)
- [x] Создать миграции Flyway
- [x] Настроить Spring Security (Basic Auth)
- [x] Реализовать репозитории
- [x] Настроить Docker Compose

**Критерии приемки:**
- Проект компилируется
- БД создается с миграциями
- Тестовые пользователи работают

### Итерация 2: Core Features (неделя 2)

**Задачи:**
- [x] Реализовать ApplicationService
- [x] Реализовать Recruiter API
- [x] Реализовать HM API
- [x] Реализовать Candidate API
- [x] Реализовать Import/Export
- [x] Настроить OpenAPI/Swagger

**Критерии приемки:**
- Все API работают через Swagger
- RBAC проверен
- Импорт/экспорт работает

### Итерация 3: Polish & Deploy (неделя 3)

**Задачи:**
- [x] Реализовать Notification Worker (outbox)
- [x] Добавить валидацию и error handling
- [x] Настроить Actuator
- [x] Написать документацию
- [ ] Тестирование
- [ ] Деплой в Docker

**Критерии приемки:**
- Уведомления отправляются
- Все ошибки обрабатываются корректно
- Документация полная
- Docker образ собирается

### Метрики для сбора

**Бизнес-метрики:**
- Количество заявок по статусам
- Время в каждом статусе (среднее, медиана)
- Конверсия по воронке (NEW → APPROVED)
- Количество отклоненных заявок
- Среднее время закрытия вакансии

**Технические метрики:**
- Response time API endpoints
- Database connection pool utilization
- Notification delivery rate/failure rate
- Error rate по эндпоинтам
- JVM metrics (heap, GC)

**Реализация:**
- Spring Actuator + Micrometer
- Prometheus endpoint: `/actuator/prometheus`
- Grafana дашборды (post-MVP)

---

## H) Технические детали

### Требования к окружению

**Development:**
- Java 21 JDK
- Maven 3.8+
- Docker Desktop
- IDE (IntelliJ IDEA / Eclipse)

**Production:**
- Java 21 JRE
- PostgreSQL 17.7+
- 2GB RAM minimum
- 10GB disk minimum

### Безопасность

**Хранение паролей:**
- BCrypt с cost factor 10
- Никогда не логируются
- Seed данные с test паролями

**API Security:**
- CSRF disabled (stateless API)
- CORS может быть настроен при необходимости
- Rate limiting рекомендуется для production

**Database Security:**
- Prepared statements (защита от SQL injection)
- Минимальные привилегии для app user
- SSL для production

### Performance Considerations

**Database:**
- Connection pool (HikariCP, default 10 connections)
- Индексы на FK и часто запрашиваемых полях
- Lazy loading для связей
- Pagination для списков

**API:**
- Кэширование можно добавить для справочников
- Compression для больших ответов
- Keep-Alive connections

**Scalability:**
- Stateless design (горизонтальное масштабирование)
- Database может быть master-slave
- Notifications worker может быть отдельным pod'ом

### Мониторинг

**Health Checks:**
- `/actuator/health` - liveness probe
- Проверяет: DB connection, disk space

**Metrics:**
- `/actuator/metrics` - все метрики
- `/actuator/prometheus` - Prometheus format

**Логирование:**
- Structured logging (можно JSON для ELK)
- Correlation ID для трейсинга
- Разные уровни по окружениям

### Backup & Recovery

**Database:**
- Daily full backup
- WAL archiving для point-in-time recovery
- Retention 30 days

**Application:**
- Stateless, не требует backup
- Configuration в git
- Secrets в vault/secrets manager

---

## Допущения и ограничения MVP

### Допущения

1. **Email провайдер:** В MVP email только логируется, не отправляется
2. **Внешняя ATS:** Принимает CSV в определенном формате
3. **Портал заявок:** Экспортирует данные в Excel 2 раза в неделю
4. **Резюме:** Хранятся локально, путь в БД (S3 в будущем)
5. **Интервью:** Планируются вручную, система только фиксирует

### Ограничения MVP

1. Нет UI, только API
2. Basic Auth вместо OAuth2
3. Нет полнотекстового поиска
4. Нет real-time уведомлений (только email)
5. Упрощенная валидация бизнес-правил
6. Нет интеграции с календарем
7. Нет video interviewing
8. Нет ML для автоматического скрининга

### Будущие улучшения

**Phase 2:**
- OAuth2/JWT
- React/Angular UI
- Elasticsearch для поиска
- WebSocket для real-time
- S3 для резюме
- Kafka для events

**Phase 3:**
- ML scoring модель
- Chatbot для кандидатов
- Календарная интеграция
- Video interviewing
- Analytics dashboard
- Mobile app

---

## Заключение

Данная архитектура представляет собой production-ready MVP для автоматизации процесса рекрутинга стажеров. Она:

- ✅ Решает ключевые болевые точки (ручная работа, непрозрачность)
- ✅ Масштабируема (stateless, modular monolith → microservices)
- ✅ Безопасна (RBAC, audit, password hashing)
- ✅ Наблюдаема (actuator, metrics, logging)
- ✅ Расширяема (clean architecture, interfaces)

Следующие шаги:
1. Code review
2. Security scan
3. Load testing
4. Production deployment
5. User acceptance testing
