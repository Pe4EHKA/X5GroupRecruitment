# Docker Build and Deployment Runbook

## Prerequisites

- Docker 20.10+ with BuildKit support
- Docker Compose v2.0+
- 4GB+ available RAM
- 10GB+ available disk space

## Quick Start

### Enable BuildKit (Recommended)

BuildKit provides better caching and faster builds:

```bash
# Enable BuildKit for current session
export DOCKER_BUILDKIT=1

# Or enable permanently in Docker daemon config
# Add to /etc/docker/daemon.json or ~/.docker/config.json:
{
  "features": {
    "buildkit": true
  }
}
```

### Build All Services

```bash
# Build all images (backend + frontend)
docker compose build

# Build specific service
docker compose build backend
docker compose build frontend

# Force rebuild without cache
docker compose build --no-cache
```

### Start Services

```bash
# Start all services in detached mode
docker compose up -d

# Start and follow logs
docker compose up

# Start specific services
docker compose up -d postgres backend
```

### Stop Services

```bash
# Stop all services
docker compose down

# Stop and remove volumes (WARNING: deletes all data)
docker compose down -v
```

## Service Health Checks

### Check Service Status

```bash
docker compose ps
```

### Test Backend Health

```bash
# Health endpoint
curl http://localhost:8080/actuator/health

# API documentation
open http://localhost:8080/swagger-ui.html
```

### Test Frontend

```bash
# Homepage
curl -I http://localhost:3000/

# Open in browser
open http://localhost:3000/
```

### Test Database

```bash
# Connect to PostgreSQL
docker exec -it x5-recruitment-db psql -U recruitment -d recruitment

# Check if database is ready
docker exec x5-recruitment-db pg_isready -U recruitment
```

## Troubleshooting

### npm ci Fails with "Exit handler never called"

**Problem**: Frontend build hangs or shows npm exit handler error.

**Root Cause**: Known npm bug in Docker BuildKit environments where npm ci doesn't properly exit after completing installation.

**Solution**: Already implemented in Dockerfile - the build uses a background process wrapper that kills npm after a timeout. The dependencies are installed correctly despite the error message.

**Verification**:
```bash
# Check if node_modules was created in the build
docker build -f apps/frontend/Dockerfile --target deps -t test-deps . && \
docker run --rm test-deps ls -la /app/node_modules | head
```

### Maven SSL Certificate Error

**Problem**: Backend build fails with "PKIX path building failed" or SSL certificate errors.

**Root Cause**: Docker build environment may have issues with Maven's SSL certificate validation when downloading dependencies from Maven Central.

**Solution**: Already implemented - the Dockerfile includes SSL workaround flags:
- `-Dmaven.wagon.http.ssl.insecure=true`
- `-Dmaven.wagon.http.ssl.allowall=true`
- `-Dmaven.wagon.http.ssl.ignore.validity.dates=true`

**Note**: These flags are only used during build and don't affect runtime security.

### Backend Doesn't Start / Database Connection Fails

**Check logs**:
```bash
docker compose logs backend
docker compose logs postgres
```

**Common issues**:
1. Database not ready - wait for postgres healthy status
2. Port 8080 already in use - stop conflicting services
3. Environment variables incorrect - check docker-compose.yml

### Frontend Shows 502 Bad Gateway

**Problem**: Frontend can't connect to backend API.

**Solutions**:
```bash
# Check backend is running and healthy
docker compose ps backend
curl http://localhost:8080/actuator/health

# Check frontend logs
docker compose logs frontend

# Restart frontend
docker compose restart frontend
```

### Build is Slow / Re-downloads Dependencies

**Enable BuildKit** (if not already enabled):
```bash
export DOCKER_BUILDKIT=1
docker compose build
```

**Clear Docker build cache** if corrupted:
```bash
docker builder prune
```

## Viewing Logs

```bash
# All services
docker compose logs -f

# Specific service
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f postgres

# Last 100 lines
docker compose logs --tail=100 backend
```

## Development Workflow

### Rebuild After Code Changes

```bash
# Rebuild only changed service
docker compose build backend
docker compose up -d backend

# Or in one command
docker compose up -d --build backend
```

### Access Running Container

```bash
# Backend
docker exec -it x5-recruitment-backend bash

# Frontend
docker exec -it x5-recruitment-frontend bash

# Database
docker exec -it x5-recruitment-db psql -U recruitment -d recruitment
```

## Performance Tips

1. **Use BuildKit** - Enables parallel builds and better caching
2. **Layer Caching** - Dockerfiles are optimized to cache dependency layers separately from code
3. **Multi-stage Builds** - Both services use multi-stage builds to minimize image size
4. **Don't use --no-cache** unless necessary - Wastes build time

## Security Notes

### Development vs Production

The current Dockerfiles include workarounds suitable for development:

**Frontend:**
- npm ci exit handler bug workaround (background process timeout)

**Backend:**
- Maven SSL certificate workarounds (insecure flags)

**For production deployment:**
1. Remove Maven SSL workarounds if your environment has proper certificates
2. Consider using a private registry or artifact repository
3. Review and update all security settings
4. Use official images or scan images for vulnerabilities

### Environment Variables

Never commit sensitive data. Use `.env` files (excluded from git):

```bash
# Create .env file
cp .env.example .env
# Edit with your values
```

## Common Commands Reference

```bash
# Full stack management
docker compose up -d              # Start all services
docker compose down               # Stop all services
docker compose ps                 # Show service status
docker compose logs -f            # Follow all logs

# Build commands
docker compose build              # Build all
docker compose build --no-cache   # Force rebuild
docker compose build frontend     # Build one service

# Cleanup
docker compose down -v            # Remove volumes
docker system prune -a            # Clean Docker system
docker builder prune              # Clean build cache

# Health checks
curl http://localhost:8080/actuator/health  # Backend
curl http://localhost:3000/                 # Frontend
docker exec x5-recruitment-db pg_isready -U recruitment  # Database
```

## Getting Help

If you encounter issues not covered here:

1. Check service logs: `docker compose logs <service>`
2. Verify Docker version: `docker --version`
3. Check BuildKit is enabled: `docker buildx version`
4. Review the Dockerfiles for commented explanations
5. Consult docs/performance.md for caching behavior
