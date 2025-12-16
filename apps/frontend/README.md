# X5 Tech Recruitment System - Frontend

MVP UI для системы автоматизации процессов отбора и обратной связи в рамках программы стажировок.

## Технологии

- **Next.js 14+** (App Router) - React фреймворк
- **TypeScript** - типизация
- **Material UI (MUI)** - UI компоненты
- **React Query** (TanStack Query) - управление состоянием и кэширование
- **react-hook-form + zod** - формы и валидация
- **Axios** - HTTP клиент
- **Notistack** - уведомления
- **date-fns** - работа с датами

## Структура проекта

```
apps/frontend/
├── src/
│   ├── app/                    # Next.js App Router pages
│   │   ├── admin/             # Админ панель
│   │   ├── hm/                # HM интерфейс
│   │   ├── recruiter/         # Рекрутер интерфейс
│   │   ├── status/[token]/    # Публичная страница статуса кандидата
│   │   ├── login/             # Логин
│   │   ├── layout.tsx         # Root layout
│   │   └── page.tsx           # Home page
│   ├── components/            # React компоненты
│   │   ├── DashboardLayout.tsx    # Главный layout с навигацией
│   │   ├── ProtectedRoute.tsx     # Route guard
│   │   └── StatusBadge.tsx        # Badge компоненты
│   ├── hooks/                 # Custom React hooks
│   │   ├── useRecruiter.ts   # Recruiter API hooks
│   │   ├── useHm.ts          # HM API hooks
│   │   └── useCandidate.ts   # Candidate API hooks
│   ├── lib/                   # Библиотеки и утилиты
│   │   └── api.ts            # API client (axios)
│   ├── providers/             # React context providers
│   │   ├── AuthProvider.tsx  # Auth context
│   │   ├── QueryProvider.tsx # React Query provider
│   │   ├── ThemeProvider.tsx # MUI theme provider
│   │   └── SnackbarProvider.tsx # Notifications provider
│   └── types/                 # TypeScript types
│       └── index.ts          # Shared types
├── public/                    # Static files
├── next.config.js            # Next.js config
├── tsconfig.json             # TypeScript config
└── package.json              # Dependencies

```

## Страницы и функциональность

### Public / Candidate
- **`/status/[token]`** - Страница статуса кандидата
  - Отображение текущего статуса заявки
  - Визуальный прогресс (stepper)
  - Комментарии и следующие шаги

### Recruiter
- **`/recruiter/dashboard`** - Dashboard с метриками
  - Счетчики по статусам (новые, скрининг, HM review, и т.д.)
  - SLA нарушения
  - Общая статистика

- **`/recruiter/applications`** - Список заявок
  - Таблица с пагинацией и сортировкой
  - Фильтры (статус, дата, SLA)
  - Переход к детальной странице

- **`/recruiter/applications/[id]`** - Детали заявки
  - Информация о кандидате
  - История статусов
  - Действия: изменить статус, отправить на HM
  - Фидбеки от HM

- **`/recruiter/import`** - Импорт из XLSX
  - Загрузка Excel файла
  - Отображение результатов импорта
  - Таблица ошибок

- **`/recruiter/export`** - Экспорт одобренных
  - Скачивание Excel со списком одобренных кандидатов

### HM (Hiring Manager)
- **`/hm/inbox`** - Входящие заявки
  - Список заявок на рассмотрении
  - Таблица с основной информацией

- **`/hm/applications/[id]`** - Принятие решения
  - Информация о кандидате и заявке
  - Форма принятия решения (одобрить/отклонить)
  - Структурированный фидбек
  - Кадровый резерв (talent pool)

### Admin
- **`/admin/programs`** - Программы/вакансии (placeholder)
- **`/admin/templates`** - Шаблоны уведомлений (placeholder)
- **`/admin/users`** - Управление пользователями (placeholder)
- **`/admin/audit`** - Журнал аудита (placeholder)

## Установка и запуск

### Предварительные требования

- Node.js 18+ и npm 9+
- Backend запущен на `http://localhost:8080`

### Установка зависимостей

```bash
cd apps/frontend
npm install
```

### Переменные окружения

Создайте файл `.env.local` (см. `.env.local.example`):

```env
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

### Запуск в dev режиме

```bash
# Из корня монорепо
npm run frontend:dev

# Или из директории frontend
cd apps/frontend
npm run dev
```

Приложение будет доступно на `http://localhost:3000`

### Сборка для production

```bash
npm run build
npm run start
```

## Docker Build

### Building Docker Image

```bash
# From repository root
docker build -t x5-frontend -f apps/frontend/Dockerfile .

# Or using docker compose
docker compose build frontend
```

**Note on package-lock.json**: The frontend directory contains a copy of the root workspace `package-lock.json` to support standalone Docker builds. This file is kept in sync with the root lockfile and should be updated whenever frontend dependencies change.

### Running in Docker

```bash
docker compose up frontend
```

The frontend service will be available at `http://localhost:3000`.

## Авторизация (DEV режим)

В dev режиме используется упрощенная авторизация с базовой HTTP аутентификацией:

### Тестовые пользователи

```
Recruiter:
  username: recruiter
  password: recruiter123

Hiring Manager:
  username: hm
  password: hm123

Admin:
  username: admin
  password: admin123
```

### Quick Login

На странице `/login` есть кнопки быстрого входа для всех ролей.

## API Integration

### API Client

API клиент находится в `src/lib/api.ts` и использует Axios с настроенными interceptors:

- Автоматическое добавление Authorization header (Basic Auth)
- Обработка 401 ошибок с редиректом на login
- CORS поддержка

### React Query Hooks

Все API запросы выполняются через custom hooks в `src/hooks/`:

- **useRecruiter.ts** - hooks для recruiter операций
  - `useDashboardMetrics()` - метрики dashboard
  - `useApplications(filters)` - список заявок
  - `useApplication(id)` - детали заявки
  - `useChangeStatus()` - изменение статуса (mutation)
  - `useSendToHm()` - отправка на HM (mutation)
  - `useImportXlsx()` - импорт Excel (mutation)
  - `useExportApproved()` - экспорт одобренных (mutation)

- **useHm.ts** - hooks для HM операций
  - `usePendingApplications()` - входящие заявки
  - `useHmApplication(id)` - детали заявки
  - `useSubmitDecision()` - отправка решения (mutation)

- **useCandidate.ts** - hooks для кандидатов
  - `useCandidateStatus(token)` - статус по токену

### Типы

TypeScript типы для API находятся в `src/types/index.ts` и соответствуют backend DTOs.

## Route Guards

Все страницы защищены компонентом `ProtectedRoute`, который проверяет:
- Авторизацию пользователя
- Наличие необходимой роли

При отсутствии авторизации - редирект на `/login`.

## UI Компоненты

### DashboardLayout

Главный layout с:
- App bar с логотипом и профилем
- Drawer навигация (фильтруется по ролям)
- Адаптивный дизайн (mobile/desktop)

### StatusBadge

Цветные badges для отображения статусов заявок с соответствующими цветами:
- NEW - синий
- SCREENING - фиолетовый
- PENDING_HM_REVIEW - оранжевый
- APPROVED - зеленый
- REJECTED - красный

## Обработка ошибок

Ошибки API отображаются через:
- Snackbar уведомления (через notistack)
- Детальные сообщения об ошибках из backend

## Валидация форм

Все формы используют:
- react-hook-form для управления состоянием
- zod для схем валидации
- Отображение ошибок валидации

## Missing Backend APIs

Следующие эндпоинты не реализованы в текущем backend:

1. `GET /api/recruiter/dashboard/metrics` - ✅ **ДОБАВЛЕНО**
2. `GET /api/hm/pending` - нужен эндпоинт для получения pending заявок
3. Admin CRUD операции для programs/templates/users/audit

## Roadmap

### Phase 2
- [ ] OpenAPI generator для автоматической генерации типов
- [ ] MSW (Mock Service Worker) для dev режима без backend
- [ ] Реальная JWT авторизация
- [ ] E2E тесты (Playwright)
- [ ] Admin CRUD интерфейсы
- [ ] Улучшенная обработка ошибок
- [ ] Loading states и skeleton screens
- [ ] Оптимизация производительности

### Phase 3
- [ ] Real-time обновления (WebSocket)
- [ ] Notifications center
- [ ] Advanced фильтры и поиск
- [ ] Экспорт в различные форматы
- [ ] Bulk operations
- [ ] Analytics dashboard

## Разработка

### Добавление новой страницы

1. Создать файл `page.tsx` в соответствующей директории `src/app/`
2. Обернуть в `ProtectedRoute` с нужными ролями
3. Использовать `DashboardLayout` для консистентности
4. Добавить пункт меню в `DashboardLayout.tsx`

### Добавление нового API endpoint

1. Создать hook в соответствующем файле в `src/hooks/`
2. Добавить типы в `src/types/index.ts`
3. Использовать hook в компоненте
4. Обработать loading/error states

## License

Proprietary - X5 Tech
