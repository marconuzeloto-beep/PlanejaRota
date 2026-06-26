# LogiCore — Logistics Decision Engine

A full-stack logistics planning system with multi-strategy route optimization, simulation, and geospatial visualization.

## Architecture

```
PlanejaRota/
├── backend/          # Spring Boot 3 + Java 21 (Clean Architecture + DDD)
├── frontend/         # React 18 + TypeScript + Vite + Leaflet
├── docker-compose.yml
└── .env.example
```

### Backend Bounded Contexts

| Context | Responsibility |
|---|---|
| `identity` | Auth, JWT, Organizations, Users |
| `core` | Customers, Vehicles, DeliveryOrders |
| `planning` | Route aggregate, Strategy Pattern, RouteBuilder |
| `simulation` | In-memory comparison of N strategies, What-if analysis |
| `api` | REST controllers, DTOs, Exception handling |
| `shared` | Value objects, Tenant context, Exceptions |

### Planning Engine — Strategy Pattern

Three pluggable optimization strategies:

- **SHORTEST_DISTANCE_V1** — Nearest Neighbor greedy (minimize total distance)
- **PRIORITY_FIRST_V1** — Priority descending, tiebreak by distance from depot
- **HYBRID_V1** — Weighted score: `0.6 × (1 - dist/maxDist) + 0.4 × (priority/5)`

All strategies implement `OptimizationStrategy` and are auto-discovered by `StrategyRegistry`.

## Prerequisites

- Docker 24+ and Docker Compose v2
- (For local dev without Docker) Java 21, Node.js 20, PostgreSQL 16 with PostGIS

## Quick Start

```bash
# 1. Clone and configure
git clone <repo-url>
cd PlanejaRota
cp .env.example .env

# 2. Start all services
docker compose up -d

# 3. Access
#   Frontend:  http://localhost:80
#   API docs:  http://localhost:8080/swagger-ui.html
#   Metrics:   http://localhost:8080/actuator/prometheus
```

### With pgAdmin

```bash
docker compose --profile tools up -d
# pgAdmin: http://localhost:5050  (admin@logicore.dev / admin)
```

## Environment Variables

See `.env.example` for all variables. Key ones:

| Variable | Default | Description |
|---|---|---|
| `JWT_SECRET` | `logicore-dev-secret-...` | **Change in production** — min 256-bit |
| `POSTGRES_PASSWORD` | `logicore` | DB password |
| `SPRING_PROFILES_ACTIVE` | `development` | `local`, `development`, `production`, `test` |
| `CORS_ALLOWED_ORIGINS` | `http://localhost:5173` | Allowed frontend origins |

## API Overview

All endpoints require `Authorization: Bearer <JWT>` (except `/api/v1/auth/**`).

### Auth
| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/auth/register` | Register organization + admin user |
| POST | `/api/v1/auth/login` | Login → JWT |

### Core Domain
| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/customers` | Create customer |
| GET | `/api/v1/customers` | List customers |
| POST | `/api/v1/vehicles` | Create vehicle |
| GET | `/api/v1/vehicles` | List vehicles |
| POST | `/api/v1/orders` | Create delivery order |
| GET | `/api/v1/orders` | List orders |

### Planning & Simulation
| Method | Path | Description |
|---|---|---|
| POST | `/api/v1/routes/plan` | Plan route with chosen strategy → `MapRouteDTO` |
| POST | `/api/v1/routes/simulate` | Compare N strategies in-memory → `ScenarioComparison` |
| POST | `/api/v1/routes/{id}/what-if/{orderId}` | Add order to existing route → `ImpactReport` |

### Full Interactive Docs
Available at `/swagger-ui.html` (disabled in `production` profile).

## Development

### Backend (local without Docker)

```bash
cd backend
# Start PostgreSQL + Redis with Docker only
docker compose up postgres redis -d

# Run Spring Boot
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

### Frontend (local)

```bash
cd frontend
npm install
npm run dev          # http://localhost:5173
```

### Tests

```bash
# Backend — requires Docker for Testcontainers
cd backend
./mvnw test

# Frontend
cd frontend
npm test
```

## Observability

- **Health check**: `GET /actuator/health`
- **Prometheus metrics**: `GET /actuator/prometheus`
- **Custom metrics**:
  - `logicore.planning.route.duration` — route planning latency
  - `logicore.simulation.compare.duration` — strategy comparison latency
  - `logicore.simulation.whatif.duration` — what-if analysis latency
- **Structured logging**: MDC fields `requestId`, `organizationId`, `userId`, `method`, `path` on every log line

## Security

- JWT RS256/HS256 with configurable expiration (default 24h)
- BCrypt cost 12 for passwords
- Multi-tenant isolation: all queries are scoped to `organization_id` from JWT
- RFC 7807 ProblemDetail for all error responses
- Swagger UI disabled in production profile

## Tech Stack

| Layer | Technology |
|---|---|
| Backend runtime | Java 21, Spring Boot 3.3 |
| Persistence | PostgreSQL 16 + PostGIS, Hibernate Spatial, Flyway |
| Cache | Redis 7 |
| Frontend | React 18, TypeScript, Vite, Leaflet, Zustand, React Query |
| Styling | Tailwind CSS |
| Containerization | Docker Compose, multi-stage builds |
| Metrics | Micrometer + Prometheus |
| API Docs | SpringDoc OpenAPI 3 |
