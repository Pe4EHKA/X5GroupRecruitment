# API Backend - Missing Endpoints для Frontend

Этот документ описывает API эндпоинты, которые ожидает frontend, но которые отсутствуют или требуют доработки в текущем backend.

## ✅ Реализованные

### Recruiter API
- `GET /api/recruiter/dashboard/metrics` - ✅ Добавлен метрики dashboard
- `GET /api/recruiter/applications` - ✅ Список заявок с фильтрами
- `GET /api/recruiter/applications/{id}` - ✅ Детали заявки
- `PATCH /api/recruiter/applications/{id}/status` - ✅ Изменение статуса
- `POST /api/recruiter/applications/{id}/send-to-hm` - ✅ Отправка на HM

### Import/Export API
- `POST /api/import-export/import` - ✅ Импорт Excel
- `GET /api/import-export/export/approved` - ✅ Экспорт одобренных

### Candidate API
- `GET /api/candidate/status?token={token}` - ✅ Статус кандидата

### CORS
- ✅ Добавлена конфигурация CORS для работы с frontend на localhost:3000

## ⚠️ Требуют доработки

### HM API

**GET /api/hm/pending**
```
Описание: Получить список заявок, ожидающих решения HM
Текущая реализация: Endpoint отсутствует
Требуется: Добавить endpoint, возвращающий заявки со статусом PENDING_HM_REVIEW для текущего HM

Response:
{
  "content": [ApplicationDto...],
  "totalElements": 100,
  "totalPages": 10,
  "size": 10,
  "number": 0
}
```

**Workaround**: Frontend может использовать `GET /api/hm/applications` с фильтром status=PENDING_HM_REVIEW

**GET /api/hm/applications/{id}**
```
Описание: Получить детали заявки для HM
Текущая реализация: Endpoint возвращает базовую информацию
Требуется: Добавить полную информацию включая:
  - statusHistory (история статусов)
  - feedbacks (предыдущие фидбеки)
  - preferences (предпочтения кандидата)

Response: ApplicationDetail (расширенный ApplicationDto)
```

**POST /api/hm/applications/{id}/decision**
```
Описание: Принять решение по заявке
Текущая реализация: ✅ Реализовано
Тело запроса:
{
  "decision": "APPROVE" | "REJECT" | "NEEDS_INFO",
  "overallAssessment": "string",
  "strengths": "string",
  "areasForGrowth": "string",
  "recommendations": "string",
  "talentPool": boolean
}
```

### Send To HM API

**POST /api/recruiter/applications/{id}/send-to-hm**
```
Описание: Отправить заявку конкретному HM
Текущая реализация: Изменяет статус, но не назначает HM
Требуется: Добавить параметр hmId и назначение HM

Request body:
{
  "hmId": number,
  "comment": "string"
}
```

## 🔜 Планируется (не критично для MVP)

### Admin API

**Programs/Vacancies CRUD**
- `GET /api/admin/programs` - Список программ
- `POST /api/admin/programs` - Создать программу
- `PUT /api/admin/programs/{id}` - Обновить программу
- `DELETE /api/admin/programs/{id}` - Удалить программу

**Templates CRUD**
- `GET /api/admin/templates` - Список шаблонов уведомлений
- `POST /api/admin/templates` - Создать шаблон
- `PUT /api/admin/templates/{id}` - Обновить шаблон
- `DELETE /api/admin/templates/{id}` - Удалить шаблон

**Users Management**
- `GET /api/admin/users` - Список пользователей
- `POST /api/admin/users` - Создать пользователя
- `PUT /api/admin/users/{id}` - Обновить пользователя
- `PUT /api/admin/users/{id}/roles` - Изменить роли

**Audit Log**
- `GET /api/admin/audit` - Журнал аудита с фильтрами

## Рекомендации по реализации

### 1. HM Pending Endpoint
Добавить в `HmController.java`:

```java
@GetMapping("/pending")
public ResponseEntity<Page<ApplicationDto>> getPendingApplications(
        @AuthenticationPrincipal User user,
        Pageable pageable) {
    // Get applications with status PENDING_HM_REVIEW assigned to this HM
    Page<ApplicationDto> applications = hmService.getPendingApplicationsForHm(user.getId(), pageable);
    return ResponseEntity.ok(applications);
}
```

### 2. Extended Application Details
Расширить `ApplicationDto` или создать `ApplicationDetailDto`:

```java
@Data
@Builder
public class ApplicationDetailDto extends ApplicationDto {
    private List<StatusHistoryDto> statusHistory;
    private List<FeedbackDto> feedbacks;
    private List<ApplicationPreferenceDto> preferences;
}
```

### 3. Send To HM with Assignment
Обновить `SendToHmRequest`:

```java
@Data
@Builder
public class SendToHmRequest {
    @NotNull
    private Long hmId;
    private String comment;
}
```

И обновить логику в сервисе для назначения HM.

## Статус безопасности (Security)

### CORS Configuration
✅ Добавлена конфигурация CORS в `SecurityConfig.java`:
- Разрешены origins: http://localhost:3000, http://localhost:8080
- Разрешены методы: GET, POST, PUT, PATCH, DELETE, OPTIONS
- Разрешены все заголовки
- Credentials: true

### Endpoint Protection
Все эндпоинты защищены Spring Security с role-based access control (RBAC):
- `/api/recruiter/**` - RECRUITER, ADMIN
- `/api/hm/**` - HM, ADMIN
- `/api/admin/**` - ADMIN
- `/api/candidate/**` - PUBLIC (с токеном)
- `/api/import-export/**` - RECRUITER, ADMIN

## Frontend Workarounds (временные решения)

До реализации недостающих эндпоинтов, frontend использует:

1. **HM Pending**: Может использовать существующие эндпоинты с фильтрами
2. **Application Details**: Получает базовую информацию, дополнительные данные опциональны
3. **Send To HM**: Пока просто меняет статус без назначения конкретного HM
4. **Admin Pages**: Показывают placeholder с сообщением "В разработке"

## Next Steps

1. Реализовать HM pending endpoint
2. Расширить application details с историей и фидбеками
3. Добавить назначение HM в send-to-hm
4. Реализовать Admin CRUD операции (Phase 2)
