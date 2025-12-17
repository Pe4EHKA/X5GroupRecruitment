# Recruitment System Runbook

This runbook captures the quickest ways to start the platform locally or with Docker, plus the credentials and smoke checks that keep the stack healthy.

## Prerequisites
- Docker & Docker Compose (recommended path)
- Java 21, Maven 3.8+ (for local backend work)
- Node.js 18+/npm 9+ (for local frontend work)

## Environment variables
The defaults in `docker-compose.yml` and the app configs work out of the box, but these are the knobs you can override:

### Backend
- `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD` — database connection
- `SPRING_PROFILES_ACTIVE` — set to `docker` inside containers

### Frontend
- `NEXT_PUBLIC_API_URL` — browser API base URL (defaults to `http://localhost:8080`)
- `API_INTERNAL_URL` — server-side API base URL for SSR/edge (set to `http://backend:8080` in Docker)

## Starting with Docker (one command)
```bash
docker compose up --build
```
Services come up on:
- Frontend: http://localhost:3000
- Backend: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

Stop and clean up:
```bash
docker compose down         # stop
docker compose down -v      # stop and drop volumes
```

## Local development (hot reload)
```bash
# install dependencies for both apps
make install

# start Postgres + backend + frontend in one terminal
make dev
```
Individual pieces:
```bash
make db-up       # only Postgres
make backend     # backend only (expects DB running)
make frontend    # frontend only (expects backend running)
```

## Health checks
Once the stack is running, verify the basics:
```bash
# Backend health
curl -f http://localhost:8080/actuator/health

# OpenAPI JSON
curl -f http://localhost:8080/api-docs | head

# Frontend assets
curl -f http://localhost:3000/
curl -f http://localhost:3000/_next/static/chunks/ | head
```

## Seed accounts (password: `admin123` for all)
From `V2__seed_data.sql` the default users are:
- Admin — `admin` (role: ADMIN)
- Recruiter — `recruiter` (role: RECRUITER)
- Hiring manager — `hm` (role: HM)

All use bcrypt hashes of `admin123`; change them in the DB for production.

## Smoke suite
A quick backend sweep lives in `docs/smoke.sh`:
```bash
./docs/smoke.sh
```
It pings the health endpoint, OpenAPI, recruiter/admin/HM listings, and the public candidate status endpoint using the seed users.
