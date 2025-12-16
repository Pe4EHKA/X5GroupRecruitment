# X5 Internship Recruitment System

Автоматизация процессов отбора и обратной связи в рамках программы стажировок X5 Tech.

## Обзор

Система предназначена для автоматизации процесса рекрутинга стажеров, включая:
- Импорт и обработку заявок кандидатов
- Управление воронкой отбора
- Автоматизацию коммуникаций с кандидатами
- Принятие решений hiring manager'ами
- Экспорт одобренных кандидатов во внутреннюю ATS
- **Полнофункциональный веб-интерфейс (MVP)**

## Архитектура - Monorepo (TurboRepo)

Проект организован как монорепозиторий с использованием TurboRepo для удобной разработки backend и frontend вместе:

```
X5GroupRecruitment/
├── apps/
│   ├── backend/          # Spring Boot backend (Java 21)
│   └── frontend/         # Next.js frontend (TypeScript)
├── packages/             # Shared packages (future)
├── docker-compose.yml    # Docker configuration
├── Makefile             # Development commands
├── package.json         # Root package.json (workspaces)
├── turbo.json          # TurboRepo configuration
└── README.md           # This file
```

## Технологический стек

### Backend
- **Java 21** - основной язык программирования
- **Spring Boot 3.4** - фреймворк приложения
- **Spring Data JPA** - ORM и работа с БД
- **Spring Security** - аутентификация и авторизация (RBAC)
- **PostgreSQL 17.7** - основная БД
- **Flyway** - миграции БД
- **Apache POI** - импорт/экспорт Excel
- **SpringDoc OpenAPI** - документация API
- **Docker** - контейнеризация
- **Maven** - сборка проекта

### Frontend
- **Next.js 14+** (App Router) - React фреймворк
- **TypeScript** - типизация
- **Material UI (MUI)** - UI компоненты
- **React Query** (TanStack Query) - управление состоянием
- **react-hook-form + zod** - формы и валидация
- **Axios** - HTTP клиент

## Быстрый старт

### Требования

- Java 21+
- Node.js 18+ и npm 9+
- Docker и Docker Compose
- Maven 3.8+

### Установка

```bash
# Клонировать репозиторий
git clone https://github.com/Pe4EHKA/X5GroupRecruitment.git
cd X5GroupRecruitment

# Установить все зависимости
make install
```

### Запуск для разработки

```bash
# Запустить full stack (БД + Backend + Frontend) одной командой
make dev
```

После запуска будут доступны:
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **PostgreSQL**: localhost:5432

Для остановки всех сервисов:
```bash
# Нажмите Ctrl+C в терминале, затем выполните:
make down
```

### Просмотр логов

```bash
make logs
```

### Альтернативные команды запуска

```bash
# Только база данных
make db-up

# Только backend
make backend

# Только frontend
make frontend

# Docker Compose (все в контейнерах)
make docker-up

# Остановить Docker Compose
make docker-down
```

### Проверка работоспособности

После запуска проверьте работоспособность системы:

```bash
# Запустить smoke tests
./docs/smoke.sh
```

Подробнее см. [docs/smoke.md](docs/smoke.md)

### Документация

- **[docs/runbook.md](docs/runbook.md)** - Полное руководство по развертыванию и эксплуатации
- **[docs/smoke.md](docs/smoke.md)** - Руководство по smoke-тестированию
- **[docs/admin-user-management.md](docs/admin-user-management.md)** - Управление пользователями
- **[docs/upgrade-frontend.md](docs/upgrade-frontend.md)** - Обновление зависимостей frontend
- **[UPGRADE.md](UPGRADE.md)** - Обновление Java и Spring Boot

### Сборка для production

```bash
make build
```

## Веб-интерфейс (MVP)

Система включает полнофункциональный веб-интерфейс для всех ролей:

### Роли и доступ

**Candidate (Кандидат)**
- `/status/[token]` - Просмотр статуса своей заявки

**Recruiter (Рекрутер)**
- Dashboard с метриками и статистикой
- Список заявок с фильтрами и поиском
- Детальный просмотр заявок
- Изменение статусов и назначение на HM
- Импорт заявок из Excel
- Экспорт одобренных кандидатов

**HM (Hiring Manager)**
- Входящие заявки на рассмотрении
- Просмотр деталей кандидата
- Принятие решений с структурированным фидбеком
- Добавление в кадровый резерв

**Admin (Администратор)**
- Управление программами/вакансиями
- Настройка шаблонов уведомлений
- **Управление пользователями** (создание, редактирование, назначение ролей)
- Журнал аудита

### Тестовые пользователи

```
Recruiter:
  username: recruiter
  password: recruiter123

HM:
  username: hm
  password: hm123

Admin:
  username: admin
  password: admin123
```

На странице логина есть кнопки быстрого входа для удобства.

### Документация Frontend

Подробная документация по frontend находится в [apps/frontend/README.md](apps/frontend/README.md)

## Архитектура Backend

### Модульный монолит

Приложение организовано как модульный монолит с четким разделением на слои:

```
src/main/java/com/x5/recruitment/
├── api/                    # API слой (REST контроллеры, DTOs)
│   ├── controller/         # REST контроллеры
│   ├── dto/               # Data Transfer Objects
│   └── exception/         # Обработка ошибок API
├── application/           # Application слой (сервисы, use cases)
│   └── service/          # Бизнес-логика
├── domain/               # Domain слой (доменная модель)
│   ├── model/           # Entities, Value Objects
│   └── repository/      # Репозитории (интерфейсы)
└── infrastructure/      # Infrastructure слой
    ├── config/         # Конфигурация
    ├── security/       # Безопасность
    └── notification/   # Уведомления (outbox pattern)
```

### Доменная модель

Основные агрегаты:

- **Candidate** - кандидат (агрегат)
- **Application** - заявка кандидата на вакансию (главный агрегат)
- **Vacancy** - вакансия/программа стажировки
- **User** - пользователь системы (рекрутер, HM, админ)
- **StatusHistory** - история изменений статуса
- **Interview** - интервью
- **Feedback** - структурированная обратная связь
- **Notification** - уведомления (outbox pattern)

### База данных

ERD находится в файле `docs/ARCHITECTURE.md`.

Основные таблицы:
- `users`, `user_roles` - пользователи и роли
- `candidates` - кандидаты
- `vacancies` - вакансии
- `applications` - заявки
- `status_history` - история статусов
- `interviews` - интервью
- `feedbacks` - обратная связь
- `notifications` - очередь уведомлений

## API

### Swagger/OpenAPI

Документация API доступна по адресу: http://localhost:8080/swagger-ui.html

### Основные эндпоинты

#### Recruiter API (`/api/recruiter`)

```
GET    /api/recruiter/applications              - Список заявок (с фильтрами)
GET    /api/recruiter/applications/{id}         - Детали заявки
POST   /api/recruiter/applications              - Создать заявку
PATCH  /api/recruiter/applications/{id}/status  - Изменить статус
POST   /api/recruiter/applications/{id}/send-to-hm - Отправить на HM
```

#### Hiring Manager API (`/api/hm`)

```
GET    /api/hm/pending                         - Заявки на рассмотрении
GET    /api/hm/applications/{id}               - Детали заявки
POST   /api/hm/applications/{id}/decision      - Принять решение (approve/reject)
```

#### Candidate API (`/api/candidate`)

```
GET    /api/candidate/status?token={token}     - Статус заявок (по токену)
```

#### Import/Export API (`/api/import-export`)

```
POST   /api/import-export/import               - Импорт из Excel
GET    /api/import-export/export/approved      - Экспорт одобренных
```

#### Admin API (`/api/admin`)

```
GET    /api/admin/users                        - Список пользователей (с фильтрами)
GET    /api/admin/users/{id}                   - Детали пользователя
POST   /api/admin/users                        - Создать пользователя
PUT    /api/admin/users/{id}                   - Обновить профиль
PUT    /api/admin/users/{id}/roles             - Обновить роли
PUT    /api/admin/users/{id}/status            - Обновить статус (ACTIVE/DISABLED)
```

**Подробная документация**: [docs/admin-user-management.md](docs/admin-user-management.md)

### Аутентификация

Используется HTTP Basic Authentication.

Тестовые пользователи (пароли одинаковые для всех: `admin123`):

- **admin** / admin123 - роль ADMIN
- **recruiter** / recruiter123 - роль RECRUITER  
- **hm** / hm123 - роль HM

Пример запроса:
```bash
curl -u recruiter:recruiter123 http://localhost:8080/api/recruiter/applications
```

### RBAC

Роли:
- **ADMIN** - полный доступ
- **RECRUITER** - управление заявками, импорт/экспорт
- **HM** - просмотр и принятие решений по заявкам
- **CANDIDATE** - (не используется для входа, только для модели данных)

## Бизнес-процессы

### 1. Импорт заявок

```
1. Recruiter загружает Excel с заявками
2. Система:
   - Парсит файл
   - Дедуплицирует кандидатов по email
   - Создает/обновляет кандидатов
   - Создает заявки со статусом NEW
   - Отправляет уведомления кандидатам
```

### 2. Скрининг рекрутером

```
1. Recruiter просматривает заявки со статусом NEW
2. Проводит скрининг
3. Меняет статус на:
   - SCREENING → PENDING_HM_REVIEW (подходит)
   - REJECTED (не подходит)
4. Система отправляет уведомление кандидату
```

### 3. Решение HM

```
1. HM видит заявки со статусом PENDING_HM_REVIEW
2. Просматривает детали заявки
3. Принимает решение:
   - Approve → статус APPROVED
   - Reject → статус REJECTED
4. Заполняет структурированный фидбек
5. Система:
   - Сохраняет фидбек
   - Меняет статус
   - Отправляет уведомление кандидату
```

### 4. Экспорт одобренных

```
1. Recruiter запрашивает экспорт одобренных
2. Система:
   - Выбирает заявки со статусом APPROVED
   - Генерирует Excel файл
   - Возвращает файл для загрузки
3. Recruiter загружает файл во внутреннюю ATS
```

## Уведомления (Outbox Pattern)

Система использует outbox pattern для надежной доставки уведомлений:

1. При изменении статуса создается запись в таблице `notifications`
2. Фоновый worker каждые 30 секунд проверяет несоответствующие уведомления
3. Отправляет email через `EmailService`
4. Помечает как отправленные или записывает ошибку
5. Максимум 3 попытки отправки

**Примечание**: В MVP email-сервис логирует сообщения вместо реальной отправки.

## Observability

### Actuator endpoints

```
GET /actuator/health   - Health check
GET /actuator/info     - Application info
GET /actuator/metrics  - Метрики
```

### Логирование

- Уровень логирования: INFO для приложения, DEBUG для рекрутинга
- Формат: `%d{yyyy-MM-dd HH:mm:ss} - %logger{36} - %msg%n`
- Логи пишутся в консоль и могут быть перенаправлены в файл

## Тестовые данные

После запуска приложения в БД создаются тестовые данные:

- 3 пользователя (admin, recruiter, hm)
- 3 вакансии
- 3 кандидата

## Разработка

### Структура проекта

```
.
├── docs/                      # Документация
│   └── ARCHITECTURE.md       # Детальная архитектура
├── src/
│   ├── main/
│   │   ├── java/            # Java код
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/ # Flyway миграции
│   └── test/                # Тесты
├── docker-compose.yml       # Docker Compose конфигурация
├── Dockerfile              # Dockerfile приложения
├── pom.xml                # Maven конфигурация
└── README.md              # Этот файл
```

### Миграции БД

Миграции находятся в `src/main/resources/db/migration/`:
- `V1__initial_schema.sql` - начальная схема
- `V2__seed_data.sql` - тестовые данные

При запуске Flyway автоматически применяет миграции.

### Добавление новых функций

1. Создайте миграцию БД в `db/migration/`
2. Обновите доменную модель в `domain/model/`
3. Добавьте методы в репозиторий `domain/repository/`
4. Реализуйте бизнес-логику в `application/service/`
5. Создайте DTO в `api/dto/`
6. Добавьте контроллер в `api/controller/`
7. Обновите документацию

## Безопасность

- Все пароли хешируются с BCrypt
- CSRF отключен для API (stateless)
- RBAC на уровне методов с `@PreAuthorize`
- Candidate API публичен (доступ по токену)
- Actuator endpoints частично публичны (health, info)

## Производительность

- Индексы на часто используемых полях (email, status, created_at)
- Пагинация для списков
- Lazy loading для связей
- Connection pool для БД

## Масштабирование

Для production:
1. Вынести notifications в отдельный сервис с Kafka/RabbitMQ
2. Добавить Redis для кэширования
3. Настроить read replicas для PostgreSQL
4. Использовать OAuth2/JWT вместо Basic Auth
5. Добавить rate limiting
6. Настроить мониторинг (Prometheus, Grafana)
7. Добавить distributed tracing (Zipkin, Jaeger)

## Известные ограничения MVP

- Email-уведомления только логируются (не отправляются)
- Basic Auth вместо OAuth2/JWT
- Нет UI (только API)
- Упрощенная валидация
- Нет полнотекстового поиска
- Нет файлового хранилища для резюме
- Нет интеграции с внешней ATS (только экспорт)

## Roadmap

**Фаза 2:**
- OAuth2/JWT авторизация
- Полнотекстовый поиск по кандидатам
- S3-совместимое хранилище для резюме
- Веб-интерфейс для рекрутеров и HM
- Email-templates и реальная отправка
- Webhooks для интеграции с ATS

**Фаза 3:**
- ML для автоматического скрининга
- Чат-бот для кандидатов
- Календарная интеграция для интервью
- Видео-интервью
- Analytics dashboard

## Поддержка

Для вопросов и проблем создавайте issue в GitHub.

## Лицензия

Proprietary - X5 Tech
