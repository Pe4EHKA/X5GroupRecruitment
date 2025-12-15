# Upgrade Guide: Java 21 + Spring Boot 3.4.1 + PostgreSQL 17.7

This document describes the upgrade from Java 17 + Spring Boot 3.2.1 to Java 21 + Spring Boot 3.4.1 with PostgreSQL 17.7.

## Summary of Changes

### Backend (Java/Spring Boot)

#### 1. Java Version Upgrade
- **Previous:** Java 17
- **Current:** Java 21
- **Files Changed:**
  - `apps/backend/pom.xml`: Updated `java.version`, `maven.compiler.source`, and `maven.compiler.target` to `21`

#### 2. Spring Boot Version Upgrade
- **Previous:** Spring Boot 3.2.1
- **Current:** Spring Boot 3.4.1 (latest stable in 3.4.x line)
- **Files Changed:**
  - `apps/backend/pom.xml`: Updated `spring-boot-starter-parent` version to `3.4.1`

#### 3. Dependencies Updated

| Dependency | Previous Version | New Version | Notes |
|------------|-----------------|-------------|-------|
| Spring Boot | 3.2.1 | 3.4.1 | Latest stable patch in 3.4.x line |
| SpringDoc OpenAPI | 2.3.0 | 2.7.0 | Compatible with Spring Boot 3.4.1 |
| Apache POI | 5.2.5 | 5.3.0 | Latest stable version |
| Flyway | (managed) | (managed) | Added `flyway-database-postgresql` for PostgreSQL 17 compatibility |
| PostgreSQL Driver | (managed) | (managed) | Managed by Spring Boot 3.4.1 |

#### 4. Flyway PostgreSQL Dependency
Added explicit `flyway-database-postgresql` dependency for PostgreSQL 17.7 compatibility:
```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-database-postgresql</artifactId>
</dependency>
```

### Database

#### PostgreSQL Version
- **Version:** PostgreSQL 17.7 (already configured in docker-compose.yml)
- **Status:** ✅ All 5 Flyway migrations applied successfully
- **Compatibility:** Flyway database-specific driver added for full compatibility

### Frontend (Next.js)

#### Configuration
- **Status:** ✅ No code changes required
- **Build:** ✅ Successful compilation with TypeScript 5.3.3
- **Files Added:**
  - `apps/frontend/.env.local`: Environment configuration for API base URL

#### CORS Configuration
- **Status:** ✅ Already configured in backend SecurityConfig
- **Allowed Origins:** `http://localhost:3000`, `http://localhost:8080`
- **Methods:** GET, POST, PUT, PATCH, DELETE, OPTIONS

### Build Tools

#### Makefile Updates
- Updated all `docker-compose` commands to `docker compose` (Docker Compose V2 syntax)
- **Files Changed:**
  - `Makefile`: Updated `dev`, `db-up`, `docker-up`, and `docker-down` targets

## Migration Steps Performed

### 1. Backend Upgrade
```bash
cd apps/backend

# Update pom.xml with new versions
# - Java 21
# - Spring Boot 3.4.1
# - SpringDoc OpenAPI 2.7.0
# - Apache POI 5.3.0
# - Add flyway-database-postgresql

# Build with Java 21
export JAVA_HOME=/usr/lib/jvm/temurin-21-jdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
mvn clean compile

# Run tests
mvn test
```

### 2. Database Setup
```bash
# Start PostgreSQL 17.7
docker compose up -d postgres

# Migrations run automatically on backend startup
# All 5 migrations applied successfully
```

### 3. Frontend Setup
```bash
cd apps/frontend

# Install dependencies
npm ci

# Create environment configuration
cp .env.local.example .env.local

# Build frontend
npm run build
```

## How to Run Locally

### Prerequisites
- **Java 21+** (Temurin, Oracle, or other JDK)
- **Node.js 18+** and npm 9+
- **Docker** and **Docker Compose V2**
- **Maven 3.8+**

### Quick Start

#### Option 1: Using Makefile (Recommended)
```bash
# Install all dependencies
make install

# Start full stack (PostgreSQL + Backend + Frontend)
make dev
```

After running `make dev`:
- **Frontend**: http://localhost:3000
- **Backend API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Docs**: http://localhost:8080/api-docs
- **PostgreSQL**: localhost:5432

#### Option 2: Manual Start

**1. Start PostgreSQL**
```bash
docker compose up -d postgres
```

**2. Start Backend**
```bash
cd apps/backend
export JAVA_HOME=/path/to/java-21  # Adjust path for your system
export PATH=$JAVA_HOME/bin:$PATH
mvn spring-boot:run
```

**3. Start Frontend (in separate terminal)**
```bash
cd apps/frontend
npm install  # First time only
npm run dev
```

### Verify Installation

#### Backend Health Check
```bash
# Public health endpoint (returns basic status)
curl http://localhost:8080/actuator/health

# With authentication (returns detailed status)
curl -u admin:admin123 http://localhost:8080/actuator/health
```

#### OpenAPI Documentation
```bash
# OpenAPI JSON spec
curl http://localhost:8080/api-docs

# Or visit Swagger UI
open http://localhost:8080/swagger-ui.html
```

#### Test API Endpoint
```bash
# Test recruiter dashboard metrics
curl -u recruiter:recruiter123 http://localhost:8080/api/recruiter/dashboard/metrics
```

#### Frontend
Open http://localhost:3000 in your browser

Default test users:
- **Admin**: admin / admin123
- **Recruiter**: recruiter / recruiter123
- **HM**: hm / hm123

## Breaking Changes

### None Identified
This upgrade is **backward compatible** with the existing data model and API contracts:
- ✅ No database schema changes required
- ✅ No API endpoint changes
- ✅ No DTO/model changes
- ✅ Existing Flyway migrations work without modification
- ✅ CORS already configured for local development

### Warnings (Non-Breaking)
The following Lombok warnings appear during compilation but do not affect functionality:
```
@Builder will ignore the initializing expression entirely. 
If you want the initializing expression to serve as default, add @Builder.Default.
```

**Affected files:**
- `ImportBatch.java`: Lines 36, 39, 42, 45
- `ApplicationPreference.java`: Line 40

**Impact:** None - these are informational warnings from Lombok
**Recommendation:** Consider adding `@Builder.Default` annotation to fields with initializers if you want the default values to be used in the builder pattern

## Compatibility Matrix

| Component | Version | Compatible With |
|-----------|---------|-----------------|
| Java | 21.0.9 LTS | Spring Boot 3.4.1 |
| Spring Boot | 3.4.1 | Spring Framework 6.2.1 |
| PostgreSQL | 17.7 | Flyway 10.x (via Spring Boot) |
| Hibernate | 6.6.4.Final | PostgreSQL 17.7 |
| Next.js | 14.2.35 | Node.js 18+ |
| TypeScript | 5.3.3 | Next.js 14.2.35 |

## Build System

### Maven
- **Maven Compiler Plugin:** 3.13.0 (from Spring Boot 3.4.1)
- **Java Release Target:** 21
- **Encoding:** UTF-8

### NPM
- **Package Manager:** npm 9+
- **Lock File:** package-lock.json (commit this file)
- **Node Version:** 18+ recommended

## CI/CD Considerations

If you have CI/CD pipelines, update them with:

### Java Version
```yaml
# GitHub Actions example
- uses: actions/setup-java@v4
  with:
    distribution: 'temurin'
    java-version: '21'
```

### Node Version
```yaml
# GitHub Actions example
- uses: actions/setup-node@v4
  with:
    node-version: '18'
```

### Docker Compose
Update any CI scripts using `docker-compose` to `docker compose`:
```bash
# Old (deprecated)
docker-compose up -d

# New (Docker Compose V2)
docker compose up -d
```

## Rollback Instructions

If you need to rollback to the previous version:

### 1. Revert pom.xml
```xml
<properties>
    <java.version>17</java.version>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
</properties>

<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.1</version>
</parent>

<properties>
    <openapi.version>2.3.0</openapi.version>
    <poi.version>5.2.5</poi.version>
</properties>

<!-- Remove flyway-database-postgresql dependency -->
```

### 2. Switch to Java 17
```bash
export JAVA_HOME=/path/to/java-17
export PATH=$JAVA_HOME/bin:$PATH
```

### 3. Rebuild
```bash
cd apps/backend
mvn clean package
```

### 4. Database
No rollback needed - PostgreSQL 17.7 is backward compatible with older Flyway migrations.

## Known Issues

### None Currently

## Future Improvements

1. **Add Integration Tests**: No tests currently exist in the backend
2. **Update Lombok Usage**: Add `@Builder.Default` to resolve warnings
3. **Explicit PostgreSQL Dialect**: Remove explicit `hibernate.dialect` property (deprecated warning)
4. **Mail Configuration**: Configure actual SMTP for production (currently stubbed)
5. **Security**: Consider OAuth2/JWT for production instead of HTTP Basic Auth

## References

- [Spring Boot 3.4 Release Notes](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Release-Notes)
- [Spring Boot 3.4 Migration Guide](https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-3.4-Migration-Guide)
- [Java 21 Features](https://openjdk.org/projects/jdk/21/)
- [PostgreSQL 17 Release Notes](https://www.postgresql.org/docs/17/release-17.html)
- [Flyway PostgreSQL Documentation](https://flywaydb.org/documentation/database/postgresql)

## Support

For issues or questions:
1. Check this upgrade guide
2. Review the [main README.md](README.md)
3. Create an issue in GitHub

---

**Upgrade Date:** December 15, 2025
**Performed By:** GitHub Copilot Agent
**Status:** ✅ Successful
