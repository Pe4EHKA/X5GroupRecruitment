# X5 Tech Recruitment System - Deployment Guide

Руководство по развертыванию системы для разработки, тестирования и production.

## Содержание

1. [Архитектура](#архитектура)
2. [Требования](#требования)
3. [Development Setup](#development-setup)
4. [Production Deployment](#production-deployment)
5. [Troubleshooting](#troubleshooting)

## Архитектура

```
┌─────────────────┐
│   Browser       │
└────────┬────────┘
         │ HTTP
         ↓
┌─────────────────┐
│  Next.js        │  Port 3000
│  Frontend       │  (Node.js)
└────────┬────────┘
         │ REST API
         ↓
┌─────────────────┐
│  Spring Boot    │  Port 8080
│  Backend        │  (Java 21)
└────────┬────────┘
         │ JDBC
         ↓
┌─────────────────┐
│  PostgreSQL     │  Port 5432
│  Database       │
└─────────────────┘
```

## Требования

### Development
- **Java 21+** (OpenJDK или Oracle JDK)
- **Node.js 18+** и npm 9+
- **Docker** и Docker Compose
- **Maven 3.8+**
- **Git**

### Production
- **Java 21 Runtime**
- **Node.js 18+ Runtime**
- **PostgreSQL 17.7**
- Минимум 2GB RAM
- Минимум 5GB дискового пространства

## Development Setup

### Шаг 1: Клонирование репозитория

```bash
git clone https://github.com/Pe4EHKA/X5GroupRecruitment.git
cd X5GroupRecruitment
```

### Шаг 2: Установка зависимостей

```bash
# С использованием Makefile (рекомендуется)
make install

# Или вручную
npm install
cd apps/frontend && npm install
cd ../..
```

### Шаг 3: Запуск Development Environment

#### Вариант A: Full Stack (рекомендуется)

```bash
make dev
```

Эта команда:
1. Запускает PostgreSQL в Docker
2. Запускает Spring Boot backend
3. Запускает Next.js frontend

После запуска доступны:
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- PostgreSQL: localhost:5432

#### Вариант B: Раздельный запуск

```bash
# Терминал 1: База данных
make db-up

# Терминал 2: Backend
make backend

# Терминал 3: Frontend
make frontend
```

#### Вариант C: Docker Compose (все в контейнерах)

```bash
make docker-up
```

**Важно**: При использовании docker-compose frontend нужно запускать отдельно:
```bash
cd apps/frontend
npm run dev
```

### Шаг 4: Проверка работоспособности

1. Откройте браузер: http://localhost:3000
2. Нажмите кнопку "Recruiter" для быстрого входа
3. Проверьте dashboard с метриками
4. Перейдите в "Заявки"

## Production Deployment

### Option 1: Docker Compose (рекомендуется)

#### Создать production docker-compose

Создайте файл `docker-compose.prod.yml`:

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:17.7
    environment:
      POSTGRES_DB: recruitment
      POSTGRES_USER: recruitment
      POSTGRES_PASSWORD: ${DB_PASSWORD}
    volumes:
      - postgres_data:/var/lib/postgresql/data
    restart: unless-stopped

  backend:
    build:
      context: ./apps/backend
      dockerfile: Dockerfile
    depends_on:
      - postgres
    environment:
      SPRING_PROFILES_ACTIVE: production
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/recruitment
      SPRING_DATASOURCE_USERNAME: recruitment
      SPRING_DATASOURCE_PASSWORD: ${DB_PASSWORD}
    ports:
      - "8080:8080"
    restart: unless-stopped

  frontend:
    build:
      context: ./apps/frontend
      dockerfile: Dockerfile
    depends_on:
      - backend
    environment:
      NEXT_PUBLIC_API_BASE_URL: http://backend:8080
    ports:
      - "3000:3000"
    restart: unless-stopped

volumes:
  postgres_data:
```

#### Создать Dockerfile для Frontend

`apps/frontend/Dockerfile`:

```dockerfile
FROM node:20-alpine AS deps
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM node:20-alpine AS runner
WORKDIR /app
ENV NODE_ENV production

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY --from=builder /app/public ./public
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

USER nextjs

EXPOSE 3000

ENV PORT 3000

CMD ["node", "server.js"]
```

#### Деплой

```bash
# Установите переменные окружения
export DB_PASSWORD=your_secure_password

# Запуск
docker-compose -f docker-compose.prod.yml up -d

# Проверка логов
docker-compose -f docker-compose.prod.yml logs -f
```

### Option 2: Standalone Deployment

#### Backend

```bash
cd apps/backend
mvn clean package -DskipTests
java -jar target/internship-recruitment-system-0.0.1-SNAPSHOT.jar
```

#### Frontend

```bash
cd apps/frontend
npm run build
npm start
```

### Option 3: Kubernetes (Advanced)

TBD - для Phase 2

## Configuration

### Backend Environment Variables

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/recruitment
SPRING_DATASOURCE_USERNAME=recruitment
SPRING_DATASOURCE_PASSWORD=recruitment123

# Server
SERVER_PORT=8080

# Email (для production)
SPRING_MAIL_HOST=smtp.gmail.com
SPRING_MAIL_PORT=587
SPRING_MAIL_USERNAME=your-email@example.com
SPRING_MAIL_PASSWORD=your-password

# Security
SPRING_SECURITY_USER_NAME=admin
SPRING_SECURITY_USER_PASSWORD=admin123
```

### Frontend Environment Variables

```bash
# API URL
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080

# Production
# NEXT_PUBLIC_API_BASE_URL=https://api.x5recruitment.com
```

## Database Migration

При первом запуске Flyway автоматически создаст схему БД и добавит тестовые данные.

Для ручной миграции:

```bash
cd apps/backend
mvn flyway:migrate
```

## Monitoring

### Health Check Endpoints

```bash
# Backend health
curl http://localhost:8080/actuator/health

# Frontend (Next.js автоматически)
curl http://localhost:3000
```

### Logs

```bash
# Backend logs
tail -f apps/backend/logs/application.log

# Frontend logs (development)
# В консоли где запущен npm run dev

# Docker logs
docker-compose logs -f backend
docker-compose logs -f frontend
```

## Troubleshooting

### Backend не запускается

**Проблема**: `Connection refused` к PostgreSQL

**Решение**:
```bash
# Проверить, запущен ли PostgreSQL
docker ps

# Запустить PostgreSQL
make db-up

# Проверить соединение
psql -h localhost -U recruitment -d recruitment
```

**Проблема**: `Port 8080 already in use`

**Решение**:
```bash
# Найти процесс
lsof -i :8080

# Убить процесс
kill -9 <PID>
```

### Frontend не запускается

**Проблема**: Ошибки при `npm install`

**Решение**:
```bash
# Очистить кэш и переустановить
rm -rf node_modules package-lock.json
npm install
```

**Проблема**: CORS ошибки

**Решение**:
Проверьте, что backend запущен и CORS настроен правильно в `SecurityConfig.java`

### Database Issues

**Проблема**: Flyway миграции не применяются

**Решение**:
```bash
# Пересоздать БД
docker-compose down -v
docker-compose up -d postgres

# Подождать запуска БД
sleep 5

# Запустить backend (миграции применятся автоматически)
make backend
```

## Security Checklist (Production)

- [ ] Изменить все дефолтные пароли
- [ ] Настроить SSL/TLS (HTTPS)
- [ ] Ограничить CORS только для production домена
- [ ] Включить CSRF защиту
- [ ] Настроить firewall
- [ ] Регулярные бэкапы БД
- [ ] Мониторинг и алерты
- [ ] Логирование и аудит
- [ ] Rate limiting
- [ ] Обновления безопасности

## Performance Optimization

### Backend

- Настроить connection pool
- Включить кэширование (Redis)
- Настроить индексы БД
- Enable gzip compression

### Frontend

- Enable image optimization
- Configure CDN
- Optimize bundle size
- Enable caching headers

## Support

Для вопросов и проблем создавайте issue в GitHub.

## License

Proprietary - X5 Tech
