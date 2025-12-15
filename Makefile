# Makefile for X5 Tech Recruitment System

.PHONY: help install dev build test clean docker-up docker-down db-up backend frontend

# Default target
help:
	@echo "X5 Tech Recruitment System - Development Commands"
	@echo ""
	@echo "Setup:"
	@echo "  make install      - Install all dependencies (backend + frontend)"
	@echo ""
	@echo "Development:"
	@echo "  make dev          - Run full stack (postgres + backend + frontend)"
	@echo "  make backend      - Run backend only"
	@echo "  make frontend     - Run frontend only"
	@echo "  make db-up        - Start PostgreSQL database"
	@echo ""
	@echo "Docker:"
	@echo "  make docker-up    - Start all services with Docker Compose"
	@echo "  make docker-down  - Stop all Docker services"
	@echo ""
	@echo "Build:"
	@echo "  make build        - Build backend + frontend"
	@echo ""
	@echo "Clean:"
	@echo "  make clean        - Clean all build artifacts"

# Install dependencies
install:
	@echo "Installing root dependencies..."
	npm install
	@echo "Installing frontend dependencies..."
	cd apps/frontend && npm install
	@echo "✓ All dependencies installed"

# Development - full stack
dev:
	@echo "Starting full stack development environment..."
	@echo "1. Starting PostgreSQL..."
	docker compose up -d postgres
	@echo "2. Waiting for PostgreSQL to be ready..."
	sleep 5
	@echo "3. Starting backend and frontend..."
	@echo ""
	@echo "Backend will be available at: http://localhost:8080"
	@echo "Frontend will be available at: http://localhost:3000"
	@echo "Swagger UI: http://localhost:8080/swagger-ui.html"
	@echo ""
	@make -j2 backend frontend

# Run backend only
backend:
	@echo "Starting backend..."
	cd apps/backend && mvn spring-boot:run

# Run frontend only
frontend:
	@echo "Starting frontend..."
	cd apps/frontend && npm run dev

# Start database only
db-up:
	@echo "Starting PostgreSQL..."
	docker compose up -d postgres

# Docker commands
docker-up:
	@echo "Starting all services with Docker..."
	docker compose up -d
	@echo "✓ Services started"
	@echo "Backend: http://localhost:8080"
	@echo "Swagger: http://localhost:8080/swagger-ui.html"
	@echo "PostgreSQL: localhost:5432"

docker-down:
	@echo "Stopping all Docker services..."
	docker compose down

# Build
build:
	@echo "Building backend..."
	cd apps/backend && mvn clean package -DskipTests
	@echo "Building frontend..."
	cd apps/frontend && npm run build
	@echo "✓ Build complete"

# Clean
clean:
	@echo "Cleaning build artifacts..."
	rm -rf apps/backend/target
	rm -rf apps/frontend/.next
	rm -rf apps/frontend/out
	rm -rf node_modules
	rm -rf apps/frontend/node_modules
	@echo "✓ Clean complete"
