# X5 Internship Recruitment System

Автоматизация процессов отбора и обратной связи в рамках программы стажировок X5 Tech.

## Обзор

Система предназначена для автоматизации процесса рекрутинга стажеров, включая:
- Импорт и обработку заявок кандидатов
- Управление воронкой отбора
- Автоматизацию коммуникаций с кандидатами
- Принятие решений hiring manager'ами
- Экспорт одобренных кандидатов во внутреннюю ATS
- **Система анкетирования с поддержкой видео-интервью** (NEW)
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

- **Docker и Docker Compose** (рекомендуется) - для контейнеризованного запуска
- Java 21+ - для локальной разработки backend
- Node.js 18+ и npm 9+ - для локальной разработки frontend
- Maven 3.8+ - для локальной разработки backend

### 🐳 Запуск в Docker (Рекомендуется)

Самый простой способ запустить весь стек одной командой:

```bash
# Клонировать репозиторий
git clone https://github.com/Pe4EHKA/X5GroupRecruitment.git
cd X5GroupRecruitment

# Собрать и запустить все сервисы (БД + Backend + Frontend)
docker compose up --build
```

**Готово!** Все сервисы доступны:
- 🌐 **Frontend UI**: http://localhost:3000
- 🔧 **Backend API**: http://localhost:8080
- 📚 **Swagger UI**: http://localhost:8080/swagger-ui.html
- 🗄️ **PostgreSQL**: localhost:5432 (recruitment/recruitment123)

Для остановки:
```bash
# Нажмите Ctrl+C, затем:
docker compose down
```

Для просмотра логов:
```bash
docker compose logs -f              # все сервисы
docker compose logs -f frontend     # только frontend
docker compose logs -f backend      # только backend
```

### 💻 Запуск для разработки (без Docker)

Для разработки с hot reload:

```bash
# 1. Установить все зависимости
make install

# 2. Запустить full stack одной командой
make dev
```

После запуска будут доступны:
- **Frontend**: http://localhost:3000 (с hot reload)
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **PostgreSQL**: localhost:5432

Для остановки всех сервисов:
```bash
# Нажмите Ctrl+C в терминале, затем выполните:
make down
```

### Альтернативные команды запуска

```bash
# Только база данных
make db-up

# Только backend (требует запущенную БД)
make backend

# Только frontend (требует запущенный backend)
make frontend

# Docker Compose с пересборкой
docker compose up --build

# Docker Compose в фоновом режиме
docker compose up -d

# Остановить Docker Compose
docker compose down

# Удалить все данные (включая БД)
docker compose down -v
```

### Просмотр логов

```bash
make logs                          # все Docker сервисы
docker compose logs -f frontend    # только frontend
docker compose logs -f backend     # только backend
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

Система включает упрощенный веб-интерфейс с двумя основными ролями для MVP:

### Роли и доступ

**HR (Recruiter)**
- URL: `/hr`
- Список всех заявок с расширенными фильтрами
- Фильтрация по статусам (быстрые чипы)
- Поиск по ФИО, email, телефону
- Детальный просмотр заявок
- Изменение статусов
- Импорт заявок из Excel
- Экспорт одобренных кандидатов

**Stager (Стажёр/Кандидат)**
- URL: `/stager`
- Просмотр своей заявки
- История изменения статусов (timeline)
- Текущий статус и комментарии
- Read-only доступ

**Admin (Администратор)** - для служебных целей
- URL: `/admin`
- Управление программами/вакансиями
- Настройка шаблонов уведомлений
- Управление пользователями
- Журнал аудита

**Candidate (публичный доступ)**
- URL: `/status/[token]` - Просмотр статуса по токену (без авторизации)

### Тестовые пользователи

Система поддерживает 4 роли пользователей. Все тестовые пользователи предварительно созданы в базе данных:

```
Admin:
  username: admin
  password: admin123
  роль: ADMIN
  -> перенаправление на /admin/programs
  -> доступ: управление пользователями, программами, аудит

HR/Recruiter:
  username: recruiter
  password: recruiter123
  роль: RECRUITER
  -> перенаправление на /hr
  -> доступ: просмотр и управление заявками, импорт/экспорт

Hiring Manager:
  username: hm
  password: hm123
  роль: HM
  -> перенаправление на /hm/inbox
  -> доступ: принятие решений по кандидатам, фидбек

Stager (Intern):
  username: stager
  password: stager123
  роль: STAGER
  -> перенаправление на /stager
  -> доступ: просмотр статуса своей заявки
```

**Важно:** После логина роль определяется автоматически по учётной записи пользователя через `/api/auth/me`. Выбор роли при входе **отсутствует**.

На странице логина есть кнопки быстрого входа для удобства (только в DEV режиме).

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

#### Auth API (`/api/auth`)

```
GET    /api/auth/me                             - Получить информацию о текущем пользователе (id, username, email, roles)
```

#### HR API (`/api/hr`) - Новый упрощённый интерфейс для MVP

```
GET    /api/hr/applications                     - Список заявок с расширенной фильтрацией
       Query params:
         - statuses: список статусов (multi-value)
         - vacancyId: ID вакансии
         - dateFrom: дата начала (YYYY-MM-DD)
         - dateTo: дата окончания (YYYY-MM-DD)
         - search: поиск по ФИО/email/телефону
         - page: номер страницы (0-based)
         - size: размер страницы
       
GET    /api/hr/applications/{id}                - Детали заявки
POST   /api/hr/applications/{id}/status         - Изменить статус
       Body: { status: "NEW", comment: "..." }

# Questionnaire System (NEW)
POST   /api/hr/vacancies/{id}/questions         - Создать вопрос для вакансии
PUT    /api/hr/vacancies/questions/{id}         - Обновить вопрос
GET    /api/hr/vacancies/{id}/questions         - Список вопросов вакансии
DELETE /api/hr/vacancies/questions/{id}         - Удалить вопрос
PUT    /api/hr/vacancies/{id}/questions/reorder - Изменить порядок вопросов
GET    /api/hr/applications/{id}/statistics     - Статистика по заявке
GET    /api/hr/vacancies/{id}/statistics        - Статистика по всем заявкам вакансии
```

**Подробная документация**: [docs/QUESTIONNAIRE_API.md](docs/QUESTIONNAIRE_API.md)

#### Stager API (`/api/stager`) - Новый интерфейс для стажёров

```
GET    /api/stager/application                  - Получить свои заявки
GET    /api/stager/application/{id}             - Получить конкретную заявку (с проверкой владения)
GET    /api/stager/profile                      - Получить свой профиль
PUT    /api/stager/profile                      - Обновить свой профиль
       Body: { phone, city, university, course, telegram, birthYear }

# Questionnaire System (NEW)
GET    /api/stager/application/{id}/questionnaire - Получить анкету для заявки
POST   /api/stager/application/{id}/answers       - Отправить ответы (batch)
GET    /api/stager/application/{id}/answers       - Получить свои ответы
```

#### Media API (`/api/media`) - Работа с видео (NEW)

```
POST   /api/media/upload                        - Загрузить видео
GET    /api/media/{id}                          - Метаданные медиа (с транскрипцией)
GET    /api/media/{id}/stream                   - Стриминг видео

```

#### Recruiter API (`/api/recruiter`) - Legacy, сохранён для совместимости

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

#### Candidate API (`/api/candidate`) - Публичный доступ

```
GET    /api/candidate/status?token={token}     - Статус заявок (по токену, без авторизации)
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

Тестовые пользователи:

- **recruiter** / recruiter123 - роль RECRUITER (HR в MVP)
- **stager** / stager123 - роль STAGER (Стажёр в MVP)
- **admin** / admin123 - роль ADMIN
- **hm** / hm123 - роль HM (устаревшая, для совместимости)

Пример запроса:
```bash
# Получить информацию о текущем пользователе
curl -u recruiter:recruiter123 http://localhost:8080/api/auth/me

# Получить список заявок (HR)
curl -u recruiter:recruiter123 http://localhost:8080/api/hr/applications

# Получить свои заявки (Stager)
curl -u stager:stager123 http://localhost:8080/api/stager/application
```

### RBAC

Роли в MVP:
- **RECRUITER** (HR в UI) - управление заявками, фильтрация, изменение статусов, импорт/экспорт
- **STAGER** - просмотр своих заявок, истории статусов, обновление профиля
- **ADMIN** - полный доступ ко всем функциям
- **HM** - просмотр и принятие решений по заявкам (устаревшая роль, оставлена для совместимости)
- **CANDIDATE** - публичный доступ по токену без авторизации

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

## Устранение неполадок

### Frontend не загружается / 404 на JS файлах

**Проблема:** При открытии http://localhost:3000 видите ошибки 404 на файлах `/_next/static/chunks/*.js`

**Решение:**
1. Пересоберите frontend контейнер:
   ```bash
   docker compose build frontend
   docker compose up -d frontend
   ```

2. Проверьте логи frontend:
   ```bash
   docker compose logs frontend
   ```

3. Убедитесь, что контейнер запустился успешно:
   ```bash
   docker compose ps
   # STATUS должен быть "Up" или "healthy"
   ```

### Backend не стартует / ошибки подключения к БД

**Проблема:** Backend падает с ошибками соединения с PostgreSQL

**Решение:**
1. Проверьте, что PostgreSQL запустилась и healthy:
   ```bash
   docker compose ps postgres
   # STATUS должен быть "Up (healthy)"
   ```

2. Если PostgreSQL не healthy, перезапустите:
   ```bash
   docker compose restart postgres
   docker compose restart backend
   ```

### Порты уже заняты

**Проблема:** Ошибка `Bind for 0.0.0.0:3000 failed: port is already allocated`

**Решение:**
1. Найдите процесс, занимающий порт:
   ```bash
   # Linux/Mac
   lsof -i :3000    # для frontend
   lsof -i :8080    # для backend
   lsof -i :5432    # для postgres
   
   # Windows
   netstat -ano | findstr :3000
   ```

2. Остановите процесс или измените порты в `docker-compose.yml`

### SSL/Certificate ошибки при сборке

**Проблема:** Maven или npm падают с ошибками SSL сертификатов

**Решение:** Dockerfiles уже содержат обход SSL для enterprise окружений:
- Frontend: `npm config set strict-ssl false`
- Backend: `-Dmaven.resolver.transport=wagon`

Если проблема сохраняется, проверьте корпоративный прокси.

### Очистка и пересборка

Если ничего не помогает, полная очистка:

```bash
# Остановить все контейнеры
docker compose down

# Удалить volumes (ВНИМАНИЕ: удалит все данные БД!)
docker compose down -v

# Очистить Docker build cache
docker builder prune -a

# Пересобрать все с нуля
docker compose build --no-cache
docker compose up
```

### Проверка работоспособности

Убедитесь, что все работает:

```bash
# Проверка frontend
curl http://localhost:3000
# Должен вернуть HTML с <!DOCTYPE html>

# Проверка backend
curl http://localhost:8080/actuator/health
# Должен вернуть: {"status":"UP"}

# Проверка статики frontend
curl -I http://localhost:3000/_next/static/chunks/webpack-*.js
# Должен вернуть: HTTP/1.1 200 OK

# Или запустите smoke tests
./docs/smoke.sh
```

## Известные ограничения MVP

- Email-уведомления только логируются (не отправляются)
- Basic Auth вместо OAuth2/JWT
- ~~Нет UI (только API)~~ - **Добавлен веб-интерфейс**
- Упрощенная валидация
- Нет полнотекстового поиска
- ~~Нет файлового хранилища для резюме~~ - **Добавлено хранилище для видео**
- Нет интеграции с внешней ATS (только экспорт)
- Транскрипция видео использует заглушку (готово для интеграции с реальным API)

## Новые возможности

### ✨ Система анкетирования (декабрь 2025)

Полнофункциональная система анкетирования для стажеров:

- **Гибкий конструктор вопросов** - 6 типов вопросов (текст, число, дата, выбор, видео)
- **Обязательные и опциональные вопросы** - с умной рандомизацией
- **Валидация ответов** - настраиваемые правила для каждого вопроса
- **Видео-интервью** - запись в браузере с автоматической транскрипцией
- **Статистика и фильтрация** - детальная аналитика по ответам
- **Стабильные анкеты** - фиксация вопросов на момент подачи заявки

**Документация**: 
- [QUESTIONNAIRE_API.md](docs/QUESTIONNAIRE_API.md) - API документация
- [QUESTIONNAIRE_FEATURE.md](docs/QUESTIONNAIRE_FEATURE.md) - Описание функционала
- [IMPLEMENTATION_SUMMARY.md](docs/IMPLEMENTATION_SUMMARY.md) - Технические детали

## Roadmap

**Фаза 2:**
- OAuth2/JWT авторизация
- Полнотекстовый поиск по кандидатам
- S3-совместимое хранилище для медиа-файлов
- ~~Веб-интерфейс для рекрутеров и HM~~ - **Реализовано**
- Email-templates и реальная отправка
- Webhooks для интеграции с ATS
- **Интеграция с реальным API транскрипции** (Google Speech-to-Text, AWS Transcribe)
- **Frontend для системы анкетирования**

**Фаза 3:**
- ML для автоматического скрининга
- Чат-бот для кандидатов
- Календарная интеграция для интервью
- ~~Видео-интервью~~ - **Реализовано**
- Analytics dashboard
- **Sentiment analysis для ответов на вопросы**
- **Автоматический скоринг кандидатов**

## Поддержка

Для вопросов и проблем создавайте issue в GitHub.

## Лицензия

Proprietary - X5 Tech
