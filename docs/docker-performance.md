# Docker Build Performance and Caching

This document explains how Docker layer caching works in this project and what triggers rebuilds.

## Build Optimization Strategy

Both frontend and backend Dockerfiles are optimized using:

1. **Multi-stage builds** - Separate dependency installation from code compilation
2. **Layer ordering** - Most stable (dependencies) first, most volatile (source code) last
3. **Explicit COPY operations** - Copy only what's needed for each step
4. **BuildKit features** - Use cache mounts for package managers (when stable)

## Frontend (Next.js) Caching

### Dockerfile Stages

```
deps stage    → Install node_modules
builder stage → Copy source and build
runner stage  → Production runtime image
```

### What Triggers Rebuilds

| Change | Layers Rebuilt | Time Impact | Explanation |
|--------|---------------|-------------|-------------|
| `apps/frontend/src/**/*.tsx` | builder, runner | ~30-60s | Source code change triggers build |
| `apps/frontend/package.json` | deps, builder, runner | ~2-4min | Dependencies change requires npm install |
| `apps/frontend/package-lock.json` | deps, builder, runner | ~2-4min | Lockfile change requires npm install |
| `apps/frontend/public/*` | runner only | ~5s | Only affects runtime copy |
| `apps/frontend/Dockerfile` | All | ~3-5min | Dockerfile changes rebuild everything |

### Cache Efficiency

**Optimal scenario** (only source code changed):
```bash
# First build
docker compose build frontend
# Time: ~3-4 minutes

# Modify src/pages/index.tsx
touch apps/frontend/src/pages/index.tsx

# Rebuild
docker compose build frontend
# Time: ~30-60 seconds (deps layer cached!)
```

**Dependency change scenario**:
```bash
# Modify package.json
npm install --prefix apps/frontend some-new-package

# Rebuild
docker compose build frontend
# Time: ~2-4 minutes (must reinstall all deps)
```

### Layer Details

**deps stage** (cached unless package files change):
```dockerfile
COPY apps/frontend/package.json apps/frontend/package-lock.json ./
RUN sh -c 'npm ci ...'  # Background process to work around npm bug
```

**builder stage** (rebuilds when source changes):
```dockerfile
COPY --from=deps /app/node_modules ./node_modules
COPY apps/frontend ./
RUN npm run build
```

### Known Issues and Workarounds

**npm ci "Exit handler never called" error:**
- **Issue**: npm ci hangs in Docker BuildKit environments after successfully installing dependencies
- **Impact**: Appears as error but dependencies ARE installed correctly
- **Workaround**: Dockerfile runs npm ci in background with timeout to force completion
- **Verification**: Check `node_modules` directory exists in built image

## Backend (Spring Boot/Maven) Caching

### Dockerfile Stages

```
builder stage → Install Maven, download deps, compile
runtime stage → JRE with compiled JAR only
```

### What Triggers Rebuilds

| Change | Layers Rebuilt | Time Impact | Explanation |
|--------|---------------|-------------|-------------|
| `apps/backend/src/**/*.java` | builder, runtime | ~45-90s | Source code triggers compilation |
| `apps/backend/pom.xml` | builder, runtime | ~2-5min | POM change requires dependency download |
| `apps/backend/Dockerfile` | All | ~3-6min | Dockerfile changes rebuild everything |

### Cache Efficiency

**Optimal scenario** (only source code changed):
```bash
# First build
docker compose build backend
# Time: ~3-6 minutes

# Modify Java source
touch apps/backend/src/main/java/com/x5/recruitment/Application.java

# Rebuild
docker compose build backend  
# Time: ~45-90 seconds (Maven deps cached!)
```

**Dependency change scenario**:
```bash
# Modify pom.xml
# Add new dependency to apps/backend/pom.xml

# Rebuild
docker compose build backend
# Time: ~3-5 minutes (must re-download dependencies)
```

### Layer Details

**Builder stage dependency caching**:
```dockerfile
COPY apps/backend/pom.xml ./pom.xml
# Maven dependencies cached at this layer
```

**Builder stage compilation**:
```dockerfile
COPY apps/backend/src ./src
RUN mvn clean package -DskipTests -B ... && mv target/*.jar app.jar
```

The Maven local repository (`.m2`) benefits from Docker layer caching - once dependencies are downloaded, they remain cached until `pom.xml` changes.

### Known Issues and Workarounds

**Maven SSL Certificate Errors:**
- **Issue**: Docker build environment has SSL certificate validation issues with Maven Central
- **Impact**: Build fails with "PKIX path building failed" 
- **Workaround**: Maven flags to bypass SSL validation (development only):
  ```
  -Dmaven.wagon.http.ssl.insecure=true
  -Dmaven.wagon.http.ssl.allowall=true
  -Dmaven.wagon.http.ssl.ignore.validity.dates=true
  ```
- **Production Note**: Remove these flags in production if your environment has proper certificates

## Docker Compose Build Context

Both services use the repository root as build context:

```yaml
services:
  backend:
    build:
      context: .                          # Root of repo
      dockerfile: ./apps/backend/Dockerfile
  frontend:
    build:
      context: .                          # Root of repo
      dockerfile: ./apps/frontend/Dockerfile
```

This allows Dockerfiles to access files from the root, but requires careful `.dockerignore` configuration.

## .dockerignore Files

### Root `.dockerignore`
Excludes common files when building from root context:
- Git files (`.git/`, `.github/`)
- Dependencies (`node_modules/`, `target/`)
- Build artifacts (`.next/`, `dist/`, `build/`)
- IDE files (`.idea/`, `.vscode/`)
- Documentation files (`*.md`, `docs/`)

### Service-specific `.dockerignore`
Each service has additional exclusions:

**Frontend** (`apps/frontend/.dockerignore`):
- Build outputs: `.next/`, `dist/`
- Test artifacts: `coverage/`, `playwright-report/`
- Temporary files: `*.log`, `.cache/`

**Backend** (`apps/backend/.dockerignore`):
- Maven artifacts: `target/`, `.m2/`
- IDE files: `*.iml`, `.settings/`

## Build Performance Benchmarks

Approximate build times on a modern development machine:

### First Build (cold cache)
- Frontend: 3-4 minutes
- Backend: 3-6 minutes
- Total: 6-10 minutes

### Rebuild (source-only changes, warm cache)
- Frontend: 30-60 seconds
- Backend: 45-90 seconds
- Total: 1.5-2.5 minutes

### Rebuild (dependency changes)
- Frontend: 2-4 minutes
- Backend: 2-5 minutes
- Total: 4-9 minutes

### Full Rebuild (--no-cache)
- Frontend: 3-5 minutes
- Backend: 4-7 minutes
- Total: 7-12 minutes

## BuildKit Cache Mounts

**Note**: Cache mounts are currently disabled in frontend due to npm ci bug. They are partially used in backend via layer caching.

When stable, cache mounts provide persistent caching across builds:

```dockerfile
# Example (not currently active for npm due to bug)
RUN --mount=type=cache,target=/root/.npm \
    npm ci
```

Benefits:
- Package manager cache persists between builds
- Faster dependency downloads
- Shared across different builds

## Optimization Tips

### 1. Minimize Layer Changes
- Keep `package.json`/`pom.xml` stable
- Group related `RUN` commands
- Order commands from least to most frequently changed

### 2. Use BuildKit
```bash
export DOCKER_BUILDKIT=1
```

### 3. Leverage Build Cache
```bash
# Good: Only rebuilds changed service
docker compose build frontend

# Wasteful: Rebuilds everything
docker compose build --no-cache
```

### 4. Parallel Builds
BuildKit automatically parallelizes independent build steps.

### 5. Image Size vs Build Speed
Multi-stage builds provide:
- Small runtime images (only production assets)
- Cached builder stages (fast rebuilds)

## Monitoring Cache Usage

### Check Docker Disk Usage
```bash
docker system df
```

### View Build Cache
```bash
docker buildx du
```

### Clean Build Cache
```bash
# Remove unused cache
docker builder prune

# Remove all cache (use sparingly!)
docker builder prune -a
```

## Expected Build Flow

### Scenario 1: First Time Build
```
Step 1: Download base images (node:20-slim, eclipse-temurin:21-jdk-jammy)
Step 2: Install system packages (curl, maven)
Step 3: Copy dependency files (package.json, pom.xml)
Step 4: Download/install dependencies (npm ci, mvn package)
Step 5: Copy source code
Step 6: Build application (next build, mvn package)
Step 7: Create runtime image
```

### Scenario 2: Source Code Change
```
Step 1-4: CACHED (no changes to dependencies)
Step 5: Copy new source code
Step 6: Build application with new code
Step 7: Create new runtime image
```

### Scenario 3: Dependency Change
```
Step 1-3: CACHED (base images, system packages, dependency files layer)
Step 4: Re-download/reinstall dependencies (package.json/pom.xml changed)
Step 5: Copy source code
Step 6: Build application
Step 7: Create runtime image
```

## Troubleshooting Slow Builds

**Problem**: Builds are always slow, even for small changes

**Checklist**:
1. ✓ Is BuildKit enabled? `export DOCKER_BUILDKIT=1`
2. ✓ Are you modifying `package.json`/`pom.xml` unnecessarily?
3. ✓ Is Docker Desktop configured with enough resources? (4GB+ RAM)
4. ✓ Is disk space available? `docker system df`
5. ✓ Are you using `--no-cache` flag? (removes all caching benefits)

**Solutions**:
- Enable BuildKit
- Keep dependency files stable
- Increase Docker resources
- Clean old images: `docker system prune`
- Use targeted builds: `docker compose build <service>`

## Summary

| Best Practice | Benefit |
|--------------|---------|
| Use BuildKit | Parallel builds, better caching |
| Don't use `--no-cache` | Preserves layer cache |
| Keep `package.json`/`pom.xml` stable | Maximizes dependency cache hits |
| Order Dockerfile commands correctly | Stable layers first, volatile last |
| Use multi-stage builds | Smaller images, cached build stages |
| Monitor disk usage | Prevent cache bloat |

By following these practices, you can minimize build times and maximize Docker's caching benefits.
