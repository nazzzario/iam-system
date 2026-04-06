# IAM System — CLAUDE.md

## What is this project
Diploma project: IAM system for microservice architecture.
Centralized authentication via Keycloak (OAuth2 + OIDC + JWT).

## Services
| Service       | Port | Role                        |
|---------------|------|-----------------------------|
| Keycloak      | 8080 | Auth Server (do not modify) |
| api-gateway   | 8888 | JWT filter + routing        |
| user-service  | 8081 | Resource Server, user data  |
| PostgreSQL    | 5432 | DB for user-service         |
| Keycloak DB   | 5433 | DB for Keycloak             |

## Golden Rules
1. Keycloak owns all credentials and roles — never duplicate in services
2. JWT validation happens at api-gateway AND user-service (defense in depth)
3. All services are stateless — no sessions
4. Inter-service calls use Bearer token forwarding
5. Never hardcode secrets — always use env vars

## Realm name: iam-realm
## Client id:  iam-client

## JWT Structure (Keycloak tokens)

Roles claim path:  realm_access.roles  (realm-level roles — use these)
User ID claim:     sub                 (UUID, keycloakId in local DB)
Email claim:       email
Username claim:    preferred_username

Example decoded JWT payload:
{
  "sub": "uuid-of-user",
  "email": "user@example.com",
  "preferred_username": "john",
  "realm_access": {
    "roles": ["ROLE_USER", "ROLE_ADMIN", "offline_access"]
  }
}

## Role Mapping (Spring Security)
JwtAuthenticationConverter extracts realm_access.roles
and maps them to GrantedAuthority with prefix ROLE_

So Keycloak role "ADMIN" becomes Spring Security "ROLE_ADMIN"
Used in: @PreAuthorize("hasRole('ADMIN')")
