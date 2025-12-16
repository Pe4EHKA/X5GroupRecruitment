# Runbook: X5 Recruitment System

This runbook provides step-by-step instructions for deploying, running, and managing the X5 Recruitment System.

## Table of Contents

1. [Prerequisites](#prerequisites)
2. [Quick Start](#quick-start)
3. [Deployment Options](#deployment-options)
4. [Configuration](#configuration)
5. [Common Operations](#common-operations)
6. [Monitoring](#monitoring)
7. [Troubleshooting](#troubleshooting)
8. [Maintenance](#maintenance)

## Prerequisites

### Software Requirements

- **Java 21** (OpenJDK or Eclipse Temurin)
- **Node.js 18+** and **npm 9+**
- **Docker** and **Docker Compose V2**
- **Maven 3.8+**
- **Git**

### System Requirements

- **CPU**: 2+ cores recommended
- **RAM**: 4GB minimum, 8GB recommended
- **Disk**: 2GB free space minimum
- **Ports**: 3000 (frontend), 8080 (backend), 5432 (database)

### Installation

#### Java 21 (Ubuntu/Debian)
```bash
sudo apt update
sudo apt install -y wget apt-transport-https
wget -O - https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo apt-key add -
echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | sudo tee /etc/apt/sources.list.d/adoptium.list
sudo apt update
sudo apt install -y temurin-21-jdk
```

#### Node.js 18+ (Ubuntu/Debian)
```bash
curl -fsSL https://deb.nodesource.com/setup_18.x | sudo -E bash -
sudo apt install -y nodejs
```

#### Docker (Ubuntu/Debian)
```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
```

## Quick Start

### One-Command Deployment

The easiest way to start the entire stack (database + backend + frontend):

```bash
# Clone repository
git clone https://github.com/Pe4EHKA/X5GroupRecruitment.git
cd X5GroupRecruitment

# Install dependencies
make install

# Start everything with one command
make dev
```

After `make dev` completes:
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **PostgreSQL**: localhost:5432

Press `Ctrl+C` to stop all services, then run:
```bash
make down
```

## Deployment Options

### Option 1: Makefile (Recommended for Development)

#### Start Full Stack
```bash
make dev
```

This command:
1. Starts PostgreSQL in Docker
2. Waits for database to be ready
3. Starts backend (Java 21 + Spring Boot)
4. Starts frontend (Next.js)

#### Stop All Services
```bash
make down
```

#### View Logs
```bash
make logs
```

#### Individual Services
```bash
# Database only
make db-up

# Backend only
make backend

# Frontend only
make frontend
```

### Option 2: Docker Compose (Full Containerization)

Start all services in containers:

```bash
make docker-up
```

This builds and runs:
- PostgreSQL 17.7 container
- Backend container (Java 21)
- Frontend container (Next.js production build)

Stop all containers:
```bash
make docker-down
```

View container logs:
```bash
make docker-logs
```

### Option 3: NPM Scripts

Alternative npm-based commands:

```bash
# Install dependencies
npm install

# Start database only
npm run db:up

# Start backend
npm run backend:dev

# Start frontend
npm run frontend:dev

# Stop database
npm run db:down
```

### Option 4: Manual Start

For complete control:

**1. Start Database**
```bash
docker compose up -d postgres
```

**2. Start Backend**
```bash
cd apps/backend
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64  # Adjust for your system
export PATH=$JAVA_HOME/bin:$PATH
mvn spring-boot:run
```

**3. Start Frontend** (in new terminal)
```bash
cd apps/frontend
npm install  # First time only
npm run dev
```

## Configuration

### Environment Variables

#### Root Directory: `.env`

Copy `.env.example` to `.env` and configure:

```bash
# Database
POSTGRES_DB=recruitment
POSTGRES_USER=recruitment
POSTGRES_PASSWORD=recruitment123  # Change in production!
POSTGRES_HOST=localhost
POSTGRES_PORT=5432

# Backend
SPRING_PROFILES_ACTIVE=dev
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/recruitment

# Frontend
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080

# Java
JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64  # Adjust for your system
```

#### Frontend: `apps/frontend/.env.local`

Copy `apps/frontend/.env.local.example` to `apps/frontend/.env.local`:

```bash
NEXT_PUBLIC_API_BASE_URL=http://localhost:8080
```

### Application Profiles

Backend supports multiple Spring profiles:

- **dev** (default): Local development with localhost database
- **docker**: For running in Docker Compose
- **prod**: Production configuration (requires additional setup)

Set profile via environment variable:
```bash
export SPRING_PROFILES_ACTIVE=docker
```

Or in application.properties:
```properties
spring.profiles.active=docker
```

### Database Configuration

Database credentials are configured in:
- `docker-compose.yml` (for containers)
- `apps/backend/src/main/resources/application.properties` (for local backend)

**Default credentials (development only):**
- Database: `recruitment`
- Username: `recruitment`
- Password: `recruitment123`

⚠️ **Change these in production!**

### Ports

| Service    | Default Port | Configurable In                |
|------------|--------------|--------------------------------|
| Frontend   | 3000         | `apps/frontend/package.json`   |
| Backend    | 8080         | `application.properties`       |
| PostgreSQL | 5432         | `docker-compose.yml`           |

## Common Operations

### Build for Production

```bash
make build
```

This command:
1. Builds backend JAR: `apps/backend/target/*.jar`
2. Builds frontend: `apps/frontend/.next`

### Clean Build Artifacts

```bash
make clean
```

Removes:
- `apps/backend/target/`
- `apps/frontend/.next/`
- `apps/frontend/out/`
- `node_modules/`

### Database Operations

#### Access Database Console
```bash
docker exec -it x5-recruitment-db psql -U recruitment -d recruitment
```

#### Run SQL Query
```bash
docker exec -it x5-recruitment-db psql -U recruitment -d recruitment \
  -c "SELECT COUNT(*) FROM applications;"
```

#### Backup Database
```bash
docker exec x5-recruitment-db pg_dump -U recruitment recruitment > backup_$(date +%Y%m%d_%H%M%S).sql
```

#### Restore Database
```bash
cat backup.sql | docker exec -i x5-recruitment-db psql -U recruitment -d recruitment
```

#### Reset Database
```bash
docker compose down -v  # ⚠️ This deletes all data!
docker compose up -d postgres
# Migrations will run automatically when backend starts
```

### User Management

#### View Users
```bash
docker exec -it x5-recruitment-db psql -U recruitment -d recruitment \
  -c "SELECT username, email, active FROM users;"
```

#### Create Admin User (via SQL)
```bash
docker exec -it x5-recruitment-db psql -U recruitment -d recruitment \
  -c "INSERT INTO users (username, email, password_hash, first_name, last_name, active) 
      VALUES ('newadmin', 'newadmin@x5.ru', '\$2a\$10\$N9qo8uLOickgx2ZMRZoMye8JIk0jY2PJBhYflRJGdkLW8WdF.3rIa', 'New', 'Admin', true);
      INSERT INTO user_roles (user_id, role) VALUES ((SELECT id FROM users WHERE username='newadmin'), 'ADMIN');"
```

Note: Password hash above is for `admin123`. Generate proper hashes for production.

### Logs

#### Backend Logs
```bash
# If running with Makefile
tail -f /tmp/backend.log

# If running in Docker
docker logs -f x5-recruitment-backend

# If running with mvn directly
# Logs go to console
```

#### Frontend Logs
```bash
# Development mode - logs to console

# Docker mode
docker logs -f x5-recruitment-frontend
```

#### Database Logs
```bash
docker logs -f x5-recruitment-db
```

## Monitoring

### Health Check

```bash
curl http://localhost:8080/actuator/health
```

Expected response:
```json
{"status":"UP"}
```

### Metrics

```bash
curl http://localhost:8080/actuator/metrics
```

### Prometheus Metrics

```bash
curl http://localhost:8080/actuator/prometheus
```

### Application Info

```bash
curl http://localhost:8080/actuator/info
```

## Troubleshooting

### Backend Won't Start

**Issue: Port 8080 already in use**
```bash
# Find process using port 8080
lsof -i :8080

# Kill process
kill -9 <PID>
```

**Issue: Java version mismatch**
```bash
# Check Java version
java -version

# Should be Java 21.x.x

# Set JAVA_HOME
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

**Issue: Database connection refused**
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# If not, start it
docker compose up -d postgres

# Wait for it to be ready
until docker exec x5-recruitment-db pg_isready -U recruitment; do sleep 1; done
```

### Frontend Won't Start

**Issue: Port 3000 already in use**
```bash
# Find process
lsof -i :3000

# Kill process
kill -9 <PID>
```

**Issue: Dependencies not installed**
```bash
cd apps/frontend
rm -rf node_modules package-lock.json
npm install
```

**Issue: Build errors**
```bash
cd apps/frontend
npm run build

# Check for TypeScript errors
npx tsc --noEmit

# Check for ESLint errors
npm run lint
```

### Database Issues

**Issue: Migrations failed**
```bash
# Check migration status
docker exec -it x5-recruitment-db psql -U recruitment -d recruitment \
  -c "SELECT * FROM flyway_schema_history;"

# Reset database (⚠️ deletes all data)
docker compose down -v
docker compose up -d postgres
```

**Issue: Cannot connect to database**
```bash
# Check if container is running
docker ps | grep postgres

# Check logs
docker logs x5-recruitment-db

# Test connection
docker exec x5-recruitment-db pg_isready -U recruitment
```

### Common Error Messages

**"Failed to configure a DataSource"**
- Database is not running
- Wrong connection URL in application.properties
- Wrong credentials

**"Port already in use"**
- Another process is using the port
- Previous instance wasn't stopped properly
- Use `lsof -i :<port>` to find and kill the process

**"CORS error" in browser**
- Backend CORS configuration doesn't allow frontend origin
- Check `SecurityConfig.java` for allowed origins

**"401 Unauthorized" on API calls**
- Wrong username/password
- User doesn't exist in database
- Check credentials in request

**"403 Forbidden" on API calls**
- User doesn't have required role
- RBAC is blocking access
- Check user roles in database

## Maintenance

### Update Dependencies

#### Backend Dependencies
```bash
cd apps/backend
# Check for updates
mvn versions:display-dependency-updates

# Update Spring Boot version in pom.xml manually
```

#### Frontend Dependencies
```bash
cd apps/frontend

# Check outdated packages
npm outdated

# Update all to latest (caution!)
npm update

# Update specific package
npm install package-name@latest
```

### Backup Strategy

**Database Backups**
```bash
# Daily backup script (add to crontab)
#!/bin/bash
docker exec x5-recruitment-db pg_dump -U recruitment recruitment \
  > /backups/recruitment_$(date +%Y%m%d).sql
```

**Code Backups**
- Use Git for version control
- Regular commits and pushes
- Tag releases: `git tag -a v1.0.0 -m "Release 1.0.0"`

### Log Rotation

Configure log rotation to prevent disk space issues:

```bash
# /etc/logrotate.d/x5-recruitment
/var/log/x5-recruitment/*.log {
    daily
    rotate 7
    compress
    delaycompress
    missingok
    notifempty
}
```

### Performance Tuning

**Database Connection Pool**
Edit `application.properties`:
```properties
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
```

**JVM Options**
```bash
# Set in MAVEN_OPTS or JAVA_OPTS
export MAVEN_OPTS="-Xmx2g -Xms512m"
```

**Next.js Production Build**
```bash
cd apps/frontend
npm run build
npm start  # Production mode
```

## Security Checklist

Before production deployment:

- [ ] Change all default passwords
- [ ] Use strong database passwords
- [ ] Configure HTTPS/TLS
- [ ] Set up proper CORS origins
- [ ] Enable Spring Security in production mode
- [ ] Configure proper session management
- [ ] Set up rate limiting
- [ ] Configure firewall rules
- [ ] Enable audit logging
- [ ] Set up monitoring and alerts
- [ ] Review and update security headers
- [ ] Scan for vulnerabilities

## Support

For issues or questions:
1. Check this runbook
2. Review [README.md](../README.md)
3. Check [UPGRADE.md](../UPGRADE.md) for version changes
4. Review [docs/smoke.md](./smoke.md) for testing
5. Create an issue in GitHub

## Quick Reference

```bash
# Start everything
make dev

# Stop everything
make down

# View logs
make logs

# Build for production
make build

# Clean artifacts
make clean

# Access database
docker exec -it x5-recruitment-db psql -U recruitment -d recruitment

# Check health
curl http://localhost:8080/actuator/health

# Run smoke tests
./docs/smoke.sh
```

---

**Last Updated:** December 2025  
**Version:** 2.0  
**Status:** Production Ready
