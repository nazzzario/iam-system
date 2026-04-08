# api-gateway — CLAUDE.md

## Role
API Gateway for IAM system. Routes all incoming requests to downstream 
microservices. First line of defense — validates JWT before forwarding.

## Tech stack
- Java 21, Spring Boot 3.4.4
- Spring Cloud Gateway (reactive — WebFlux, NOT Spring MVC)
- OAuth2 Resource Server for JWT validation
- Lombok, Maven

## CRITICAL — Reactive Stack
This service uses WebFlux (reactive), NOT Spring MVC.
- Use ServerHttpRequest / ServerHttpResponse (NOT HttpServletRequest)
- Use GatewayFilter / GlobalFilter (NOT OncePerRequestFilter)
- Use Mono<Void> return types in filters
- Never import javax.servlet or jakarta.servlet
- Never use @RestController — use routing in GatewayConfig

## Architecture
- Listens on port 8888
- Routes:
  /api/v1/users/** → http://user-service:8081
  /api/v1/admin/** → http://user-service:8081
- Validates JWT on all routes except /actuator/health
- Forwards Authorization header to downstream services

## JWT Validation
- Keycloak JWKS: http://keycloak:8080/realms/iam-realm/protocol/openid-connect/certs
- Issuer: http://localhost:8080/realms/iam-realm
- Same dual-URI approach as user-service

## Environment Variables
| Variable            | Default                                                      |
|---------------------|--------------------------------------------------------------|
| SERVER_PORT         | 8888                                                         |
| USER_SERVICE_URL    | http://user-service:8081                                     |
| KEYCLOAK_ISSUER_URI | http://localhost:8080/realms/iam-realm                       |
| KEYCLOAK_JWK_URI    | http://keycloak:8080/realms/iam-realm/protocol/openid-connect/certs |

## Code Style
- Constructor injection only
- Package-private classes where possible
- No @Autowired field injection

## Do NOT
- Do NOT use Spring MVC (no @RestController, no HttpServletRequest)
- Do NOT use OncePerRequestFilter
- Do NOT add spring-boot-starter-web dependency
- Do NOT validate JWT twice (gateway validates, user-service also validates)
- Do NOT store any user data
