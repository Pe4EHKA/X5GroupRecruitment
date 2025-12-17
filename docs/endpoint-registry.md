# Карта API-эндпоинтов

Унифицированный список публичных REST-эндпоинтов, используемых фронтендом.

## Базовые правила доступа
- **Аутентификация**: HTTP Basic, ответы 401/403 возвращают структуру `{ status, code, message, timestamp }`.
- **Импорт/Экспорт**: доступны ролям `RECRUITER` и `ADMIN` (см. `SecurityConfig`).
- **HR/Recruiter**: оба пространства (`/api/hr`, `/api/recruiter`) доступны `RECRUITER`/`ADMIN`; фронт использует их параллельно.
- **Candidate**: `/api/candidate/status` публичный по токену, остальные требуют роль `CANDIDATE`.

## Аутентификация
- `GET /api/auth/me` — информация о текущем пользователе (id, username, email, displayName, roles). Требуется любая аутентификация.

## Импорт/экспорт
- `POST /api/import-export/import` — загрузка Excel (`multipart/form-data`, поле `file`). Возвращает `ImportResultDto` с `batch` (totalRows/successRows/failedRows/usersCreated/usersLinked) и `errors`.
- `GET /api/import-export/batches/{id}` — состояние пачки импорта.
- `GET /api/import-export/batches/{id}/errors` — ошибки построчно (pageable).
- `GET /api/import-export/export/approved` — Excel с одобренными заявками.

## HR API (`/api/hr`)
- `GET /api/hr/applications` — список с фильтрами `statuses`, `vacancyId`, `dateFrom`, `dateTo`, `search`, `page`, `size`, `sort`.
- `GET /api/hr/applications/{id}` — детальная карточка.
- `POST /api/hr/applications/{id}/status` — изменение статуса (body `ChangeStatusRequest`).
- `POST /api/hr/vacancies/{vacancyId}/questions` — создать вопрос.
- `PUT /api/hr/vacancies/questions/{questionId}` — обновить вопрос.
- `GET /api/hr/vacancies/{vacancyId}/questions` — список вопросов.
- `DELETE /api/hr/vacancies/questions/{questionId}` — удалить вопрос.
- `PUT /api/hr/vacancies/{vacancyId}/questions/reorder` — перестановка вопросов.
- `GET /api/hr/applications/{applicationId}/statistics` — статистика по заявке.
- `GET /api/hr/vacancies/{vacancyId}/statistics` — статистика по вакансии.

## Recruiter API (`/api/recruiter`)
- `GET /api/recruiter/dashboard/metrics` — метрики дашборда.
- `GET /api/recruiter/applications` — список (фильтр по `status`, пагинация через `page/size/sort`).
- `GET /api/recruiter/applications/{id}` — детальная карточка.
- `POST /api/recruiter/applications` — создать заявку.
- `PATCH /api/recruiter/applications/{id}/status` — изменить статус.

## Candidate API (`/api/candidate`)
- `GET /api/candidate/status?token=...` — статусы по токену (без авторизации).
- `GET /api/candidate/me/applications` — все заявки авторизованного кандидата.
- `GET /api/candidate/me/applications/{id}` — конкретная заявка.
- `GET /api/candidate/me/applications/{id}/history` — история статусов заявки.

## Admin/Media/Stager
- `GET /api/admin/users` и CRUD — управление пользователями (роль `ADMIN`).
- `POST /api/media/upload` — загрузка файла (роль `ADMIN`).
- `GET /api/stager/profile`, `POST /api/stager/profile`, `POST /api/stager/application` — действия стейджера (роль `STAGER`/`CANDIDATE`).

## Фронтенд точки входа
- AuthProvider использует `GET /api/auth/me` для логина и маршрутизации.
- Recruiter UI: `/api/recruiter/*` для дашборда и карточек, `/api/import-export/*` для импорта/экспорта.
- HR UI: `/api/hr/*` для фильтрации и статистики.
- Candidate UI: `/api/candidate/status` (публично) и `/api/candidate/me/*` (авторизация).

## Диагностика ошибок
- Любые ошибки валидации → HTTP 400 с `{ status, code, message, errors? }`.
- 401/403 → `{ status, code, message, timestamp }`, фронтенд редиректит на /login при 401.
