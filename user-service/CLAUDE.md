## Code Style

- Use **Java Records** for all DTOs (not classes)
- Use **Lombok** on entities: @Data @Builder @NoArgsConstructor @AllArgsConstructor
- Use **@Valid** on all @RequestBody parameters in controllers
- Use **ResponseEntity<?>** return type in controllers
- Extract keycloakId in controller via: `jwt.getSubject()`
- Package-private where possible — avoid unnecessary public modifiers
- No field injection (@Autowired on fields) — constructor injection only
- All custom exceptions extend RuntimeException

## Security Rules — DO NOT VIOLATE

1. **Never** use `antMatchers` — use `requestMatchers` (Spring Security 6)
2. **Never** store passwords or credentials in this service
3. JWT converter MUST extract roles from `realm_access.roles`, not `roles`
4. SecurityConfig MUST call `.jwtAuthenticationConverter(converter)` explicitly
5. `@EnableMethodSecurity` must be on SecurityConfig for @PreAuthorize to work
6. Session management MUST be `SessionCreationPolicy.STATELESS`

## Environment Variables

| Variable              | Default                                      | Description          |
|-----------------------|----------------------------------------------|----------------------|
| DB_HOST               | localhost                                    | PostgreSQL host      |
| DB_PORT               | 5432                                         | PostgreSQL port      |
| DB_NAME               | userdb                                       | Database name        |
| DB_USERNAME           | postgres                                     | DB user              |
| DB_PASSWORD           | postgres                                     | DB password          |
| KEYCLOAK_ISSUER_URI   | http://localhost:8080/realms/iam-realm        | Keycloak realm URI   |
| SERVER_PORT           | 8081                                         | App port             |

## Common Mistakes — Do NOT Do These

- Do NOT create a `WebSecurityConfigurerAdapter` — it's deprecated since Spring 6
- Do NOT use `@CrossOrigin` on controllers — handle CORS in SecurityConfig
- Do NOT call Keycloak API from this service — only validate the incoming JWT
- Do NOT add `spring-boot-starter-oauth2-client` — this is a Resource Server, not a client
- Do NOT store roles in the local database — they live in Keycloak only
- Do NOT use `HttpSession` anywhere