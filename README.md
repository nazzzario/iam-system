# 🔐 IAM System

> Diploma project — Centralized Identity & Access Management for microservice architecture.
> Built with Keycloak, Spring Boot 3, and Next.js 16.

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.4-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Next.js](https://img.shields.io/badge/Next.js-16.2.2-000000?style=flat-square&logo=nextdotjs&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-5-3178C6?style=flat-square&logo=typescript&logoColor=white)
![Keycloak](https://img.shields.io/badge/Keycloak-23.0-4D4D4D?style=flat-square&logo=keycloak&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square&logo=docker&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-336791?style=flat-square&logo=postgresql&logoColor=white)

---

## 📐 Architecture

```
                        ┌─────────────────────────────────────────────┐
                        │               Docker Network                │
                        │                 iam-network                 │
                        │                                             │
  Browser / Client      │  ┌──────────┐     ┌─────────────────────┐  │
  ─────────────────────►│  │ Frontend │────►│    API Gateway      │  │
  :3000 (Next.js)       │  │ Next.js  │     │  Spring Cloud GW    │  │
                        │  │ :3000    │     │  JWT validation     │  │
                        │  └────┬─────┘     │  :8888              │  │
                        │       │           └──────────┬──────────┘  │
                        │       │ OAuth2/OIDC           │ Bearer fwd  │
                        │       │                       ▼             │
                        │       │           ┌─────────────────────┐  │
                        │       │           │   user-service      │  │
                        │       │           │  Spring Boot MVC    │  │
                        │       │           │  Resource Server    │  │
                        │       │           │  :8081              │  │
                        │       │           └──────────┬──────────┘  │
                        │       │                      │ JPA/Flyway  │
                        │       │                      ▼             │
                        │       │           ┌─────────────────────┐  │
                        │       │           │     PostgreSQL       │  │
                        │       │           │     userdb          │  │
                        │       │           │     :5432           │  │
                        │       │           └─────────────────────┘  │
                        │       │                                     │
                        │       ▼                                     │
                        │  ┌──────────────────────┐                  │
                        │  │      Keycloak         │                  │
                        │  │   Auth Server         │                  │
                        │  │   Realm: iam-realm    │                  │
                        │  │   Client: iam-client  │                  │
                        │  │   :8080               │                  │
                        │  └──────────┬────────────┘                  │
                        │             │ JDBC                          │
                        │             ▼                               │
                        │  ┌──────────────────────┐                  │
                        │  │  Keycloak PostgreSQL  │                  │
                        │  │  keycloak DB          │                  │
                        │  │  :5433                │                  │
                        │  └──────────────────────┘                  │
                        └─────────────────────────────────────────────┘
```

**Security model — defense in depth:**
1. **Frontend** delegates authentication entirely to Keycloak via OAuth2/OIDC (NextAuth.js)
2. **API Gateway** validates the JWT signature using Keycloak's JWKS endpoint before routing
3. **user-service** re-validates the JWT as an OAuth2 Resource Server — stateless, no sessions

---

## 🧱 Tech Stack

### Backend

| Component | Technology | Version |
|---|---|---|
| Language | Java | 21 |
| Framework | Spring Boot | 3.4.4 |
| Gateway | Spring Cloud Gateway (WebFlux) | 3.4.4 |
| Auth server | Keycloak | 23.0 |
| JWT validation | Spring Security OAuth2 Resource Server | 3.4.4 |
| Persistence | Spring Data JPA + Flyway | 3.4.4 |
| API docs | springdoc-openapi (Swagger UI) | 2.8.6 |
| Keycloak admin | keycloak-admin-client | 23.0.7 |
| Database | PostgreSQL | 16 |
| Build | Maven | — |
| Utilities | Lombok | — |

### Frontend

| Component | Technology | Version |
|---|---|---|
| Framework | Next.js (App Router) | 16.2.2 |
| Language | TypeScript | 5 |
| Auth | NextAuth.js v5 (beta) | 5.0.0-beta.30 |
| Styling | Tailwind CSS v4 + shadcn/ui | 4 |
| HTTP client | Axios | 1.14.0 |
| Data fetching | TanStack React Query | 5.96.2 |
| Icons | Lucide React | — |
| Themes | next-themes (dark/light) | 0.4.6 |

---

## 🌐 Service URLs

| Service | URL | Description |
|---|---|---|
| Frontend | http://localhost:3000 | Next.js app — main entry point |
| API Gateway | http://localhost:8888 | All API requests go through here |
| user-service | http://localhost:8081 | Direct access (internal only) |
| Keycloak Admin | http://localhost:8080/admin | Realm & user management |
| Keycloak Realm | http://localhost:8080/realms/iam-realm | OIDC discovery endpoint |
| Swagger UI | http://localhost:8081/swagger-ui.html | user-service API docs |

---

## 👥 Demo Users

These accounts are pre-configured in Keycloak and seeded into the local database.

| Username | Password | Role | Notes |
|---|---|---|---|
| `admin` | `admin` | `ADMIN` | Keycloak admin console |
| `john` | `john123` | `ROLE_USER` | Regular user |
| `alice` | `alice123` | `ROLE_ADMIN` | System administrator |
| `manager` | `manager123` | `ROLE_MANAGER` | Manager — stats view only |

> **Note:** Roles live exclusively in Keycloak (`realm_access.roles`). The local database stores profile data only — never credentials or roles.

---

## 🛡️ Role-Based Access Control

| Role | Dashboard | User stats | User list | Create / edit users |
|---|---|---|---|---|
| `ROLE_USER` | ✅ Profile + quote + NBU rates | ❌ | ❌ | ❌ |
| `ROLE_MANAGER` | ✅ Stats view | ✅ (read-only) | ❌ | ❌ |
| `ROLE_ADMIN` | ✅ Full stats | ✅ | ✅ | ✅ |

---

## 🚀 Quick Start

### Prerequisites

- Docker & Docker Compose v2
- A `KEYCLOAK_CLIENT_SECRET` from your Keycloak `iam-client` configuration

### 1. Clone & configure

```bash
git clone <repo-url>
cd iam-system
cp .env.example .env
# Edit .env and set your KEYCLOAK_CLIENT_SECRET
```

### 2. Launch the full stack

```bash
docker compose up --build
```

Wait for all services to be healthy (Keycloak takes ~30 s on first boot).

### 3. Open the app

Navigate to **http://localhost:3000** and click **Login**.
You will be redirected to Keycloak — log in with any demo account above.

---

## 📡 API Reference

All requests go through the API Gateway on port **8888**.
Every endpoint (except `/actuator/health`) requires a valid Bearer token.

### User endpoints

```
GET    /api/v1/users/me            — get own profile (auto-provisioned on first login)
PUT    /api/v1/users/me            — update own first / last name
```

### Admin endpoints (require `ROLE_ADMIN`)

```
GET    /api/v1/admin/users         — list all active users
POST   /api/v1/admin/users         — create user in Keycloak + DB
PUT    /api/v1/admin/users/{id}/role      — change user's realm role
PUT    /api/v1/admin/users/{id}/activate  — re-enable a deactivated user
DELETE /api/v1/admin/users/{id}           — soft-delete (sets isActive=false + disables in Keycloak)
GET    /api/v1/admin/users/stats          — total / active / new-this-month counts (ADMIN or MANAGER)
```

Full interactive docs: **http://localhost:8081/swagger-ui.html**

---

## 🔑 JWT Structure

Keycloak tokens carry roles in `realm_access.roles`. Spring Security maps them with `ROLE_` prefix.

```json
{
  "sub": "uuid-of-user",
  "email": "user@example.com",
  "preferred_username": "john",
  "realm_access": {
    "roles": ["ROLE_USER", "offline_access"]
  }
}
```

Used as: `@PreAuthorize("hasRole('ADMIN')")`

---

## 🗂️ Project Structure

```
iam-system/
├── docker-compose.yml          # Full stack orchestration
├── .env.example                # Environment variable template
├── keycloak/
│   └── realm-export.json/      # Keycloak realm import (realm, client, roles, users)
├── api-gateway/                # Spring Cloud Gateway (WebFlux, port 8888)
│   ├── src/
│   └── Dockerfile
├── user-service/               # Spring Boot Resource Server (port 8081)
│   ├── src/
│   │   └── main/
│   │       ├── java/com/example/user/
│   │       │   ├── controller/     # UserController, AdminController
│   │       │   ├── service/        # UserService, KeycloakAdminService
│   │       │   ├── entity/         # User (JPA entity)
│   │       │   ├── dto/            # Java Records for all DTOs
│   │       │   ├── security/       # JwtAuthenticationConverter
│   │       │   └── config/         # SecurityConfig, OpenApiConfig
│   │       └── resources/
│   │           └── db/migration/   # Flyway: V1 schema, V2 seed data
│   └── Dockerfile
└── frontend/                   # Next.js 16 App Router (port 3000)
    ├── app/
    │   ├── page.tsx            # Landing page with Login
    │   ├── dashboard/          # Protected: role-aware dashboard
    │   └── admin/              # Protected: admin user management
    ├── components/             # shadcn/ui components + Navbar
    ├── lib/                    # api.ts (Axios), jwt.ts (role helpers)
    ├── auth.ts                 # NextAuth.js v5 Keycloak provider config
    └── Dockerfile
```

---

## ⚙️ Environment Variables

### user-service

| Variable | Default | Description |
|---|---|---|
| `DB_HOST` | `postgres` | PostgreSQL host |
| `DB_PORT` | `5432` | PostgreSQL port |
| `DB_NAME` | `userdb` | Database name |
| `DB_USERNAME` | `postgres` | DB user |
| `DB_PASSWORD` | `postgres` | DB password |
| `KEYCLOAK_ISSUER_URI` | `http://localhost:8080/realms/iam-realm` | Token issuer (for `iss` validation) |
| `KEYCLOAK_JWK_URI` | `http://keycloak:8080/realms/iam-realm/protocol/openid-connect/certs` | JWKS endpoint (internal Docker hostname) |
| `KEYCLOAK_CLIENT_SECRET` | — | Keycloak client secret (from `.env`) |

### api-gateway

| Variable | Default | Description |
|---|---|---|
| `SERVER_PORT` | `8888` | Gateway listen port |
| `USER_SERVICE_URL` | `http://user-service:8081` | Downstream service |
| `KEYCLOAK_ISSUER_URI` | `http://localhost:8080/realms/iam-realm` | Token issuer |
| `KEYCLOAK_JWK_URI` | `http://keycloak:8080/realms/iam-realm/protocol/openid-connect/certs` | JWKS endpoint |

### frontend

| Variable | Description |
|---|---|
| `NEXTAUTH_URL` | Public base URL (`http://localhost:3000`) |
| `NEXTAUTH_SECRET` | NextAuth.js signing secret |
| `KEYCLOAK_CLIENT_ID` | `iam-client` |
| `KEYCLOAK_CLIENT_SECRET` | Keycloak client secret |
| `KEYCLOAK_ISSUER` | Internal Docker hostname (`http://keycloak:8080/realms/iam-realm`) |
| `KEYCLOAK_ISSUER_PUBLIC` | Public hostname (`http://localhost:8080/realms/iam-realm`) — must match `iss` claim |
| `NEXT_PUBLIC_API_URL` | API Gateway URL (`http://localhost:8888`) |

---

## 🧪 Running Tests

```bash
# user-service unit + integration tests (H2 in-memory)
cd user-service
./mvnw test
```

---

## 🏗️ Design Decisions

- **Keycloak owns all identity** — user-service never stores passwords, roles live in Keycloak only
- **Dual JWT validation** — gateway rejects invalid tokens before they reach downstream services; user-service re-validates for defense in depth
- **Stateless services** — `SessionCreationPolicy.STATELESS` everywhere; no `HttpSession`
- **Auto-provisioning** — `GET /api/v1/users/me` creates a local DB record on first login using the `sub` (Keycloak UUID) as the foreign key
- **Soft deletes** — deactivating a user sets `isActive=false` in the DB and disables the account in Keycloak; data is preserved
- **Dual-hostname Keycloak URIs** — the `iss` claim contains the public hostname (`localhost:8080`) but server-side calls (JWKS, token exchange) use the internal Docker hostname (`keycloak:8080`) to avoid DNS issues inside the container network

---

## 🤖 Built with Claude Code

This project was developed with the assistance of [Claude Code](https://claude.ai/code) — Anthropic's AI-powered CLI for software engineering.
Claude Code helped design the security architecture, implement JWT role extraction, wire up the Keycloak Admin API integration, and build the role-based Next.js frontend.
