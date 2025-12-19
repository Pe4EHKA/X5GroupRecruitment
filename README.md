# X5 Internship Recruitment System

An automated recruitment and feedback system for the X5 Tech Internship Program.

## 🚀 Overview

The system automates the intern recruitment process, including:
- **Candidate Application Management**: Import and processing of candidate applications.
- **Selection Funnel**: Managing candidates through various stages (Screening, HM Review, Approved/Rejected).
- **Questionnaire System**: Customizable questionnaires with support for video interviews.
- **Automated Transcription**: Offline video-to-text transcription using the Vosk model.
- **Notifications**: Outbox pattern for reliable email notifications.
- **Web Interface**: Full-featured MVP for HR, Hiring Managers, and Candidates (Stagers).

## 🏗️ Architecture

The project is organized as a monorepo managed by **TurboRepo**:

```text
X5GroupRecruitment/
├── apps/
│   ├── backend/          # Spring Boot backend (Java 21)
│   └── frontend/         # Next.js frontend (TypeScript)
├── packages/             # Shared packages (future)
├── docs/                 # Documentation and guides
├── docker-compose.yml    # Docker configuration
├── Makefile             # Development commands
├── package.json         # Root package.json (npm workspaces)
├── turbo.json          # TurboRepo configuration
└── README.md           # This file
```

## 🛠️ Tech Stack

### Backend
- **Java 21** & **Spring Boot 3.4**
- **Spring Data JPA** & **Hibernate**
- **PostgreSQL 17.7** with **Flyway** migrations
- **Spring Security** (RBAC with Basic Auth)
- **Apache POI** (Excel Import/Export)
- **SpringDoc OpenAPI** (Swagger documentation)
- **Vosk** (Offline Speech-to-Text)
- **FFmpeg** (Audio extraction for transcription)

### Frontend
- **Next.js 14+** (App Router) & **React**
- **TypeScript**
- **Material UI (MUI)**
- **React Query** (TanStack Query)
- **react-hook-form** + **Zod**
- **Axios**

## 🚦 Getting Started

### Requirements
- **Docker & Docker Compose** (Recommended)
- **Java 21+** & **Maven 3.8+** (for local backend development)
- **Node.js 18+** & **npm 9+** (for local frontend development)

### 🐳 Running with Docker (Recommended)

The easiest way to start the entire stack:

```bash
# Clone the repository
git clone https://github.com/Pe4EHKA/X5GroupRecruitment.git
cd X5GroupRecruitment

# Build and start all services
docker compose up --build
```

**Access URLs:**
- 🌐 **Frontend UI**: [http://localhost:3000](http://localhost:3000)
- 🔧 **Backend API**: [http://localhost:8080](http://localhost:8080)
- 📚 **Swagger UI**: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- 🗄️ **PostgreSQL**: `localhost:5432` (User/Pass: `recruitment`/`recruitment123`)

### 💻 Local Development (Without Docker)

1. **Install dependencies:**
   ```bash
   make install
   ```

2. **Run full stack:**
   ```bash
   make dev
   ```

## 📜 Available Scripts

### Makefile Commands
- `make install` - Install all dependencies (npm & frontend).
- `make dev` - Run PostgreSQL, Backend, and Frontend in parallel.
- `make build` - Build backend (Maven) and frontend (Next.js).
- `make down` - Stop all services and processes.
- `make logs` - Follow Docker container logs.
- `make backend` - Run only the backend application.
- `make frontend` - Run only the frontend application.
- `make db-up` - Start only the PostgreSQL database.
- `make clean` - Remove build artifacts and `node_modules`.

### npm Scripts (Root)
- `npm run dev` - Start development mode via Turbo.
- `npm run build` - Build all apps via Turbo.
- `npm run test` - Run all tests via Turbo.
- `npm run lint` - Run linting across the monorepo.
- `npm run format` - Format code using Prettier.

## 🔑 Environment Variables

### Backend (`apps/backend`)
Key variables (mostly configured in `application.properties` or `docker-compose.yml`):
- `SPRING_DATASOURCE_URL`: JDBC URL for PostgreSQL.
- `SPRING_DATASOURCE_USERNAME`: Database username.
- `SPRING_DATASOURCE_PASSWORD`: Database password.
- `APP_TRANSCRIPTION_LOCAL_MODEL_PATH`: Path to the Vosk model directory.
- `VOSK_MODEL_PATH`: Path for Vosk within the container.
- `TRANSCRIPTION_MODEL_HOST_PATH`: Host path for the Vosk model (default: `./apps/backend/transcription-model`).

### Frontend (`apps/frontend`)
- `API_INTERNAL_URL`: Internal URL for the backend API (used during SSR).
- `NEXT_PUBLIC_API_URL`: Public URL for the backend API (client-side).

## 🧪 Testing

### Backend
Run integration and unit tests:
```bash
cd apps/backend
mvn test
```

### Frontend
Run linting and build check:
```bash
cd apps/frontend
npm run lint
npm run build
```

### Smoke Tests
Execute the smoke test script to verify core API functionality:
```bash
./docs/smoke.sh
```

## 🎙️ Offline Transcription (Vosk)

The system uses **Vosk** for offline transcription.
1. Download a model (e.g., `vosk-model-small-ru-0.22`).
2. Extract it to `apps/backend/transcription-model`.
3. Ensure the path in `APP_TRANSCRIPTION_LOCAL_MODEL_PATH` points directly to the folder containing `am`, `conf`, and `graph`.

## 👥 Test Users

| Role | Username | Password | Access |
| :--- | :--- | :--- | :--- |
| **Admin** | `admin` | `admin123` | Full system access, user management |
| **Recruiter (HR)** | `recruiter` | `recruiter123` | Application management, import/export |
| **Hiring Manager** | `hm` | `hm123` | Decision making, feedback |
| **Stager (Intern)** | `stager` | `stager123` | Personal application status |

## 📅 Roadmap / TODO
- [ ] Implement OAuth2/JWT instead of Basic Auth.
- [ ] Add S3-compatible storage for media files.
- [ ] Integrate real email service (currently logging only).
- [ ] Implement full-text search for candidates.
- [ ] Add Sentiment Analysis for candidate responses.
