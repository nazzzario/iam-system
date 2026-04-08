@AGENTS.md

# frontend — CLAUDE.md

## Role
Frontend for IAM System diploma project.
Next.js 14 app with NextAuth.js for Keycloak authentication.

## Tech stack
- Next.js 14 (App Router)
- TypeScript
- Tailwind CSS + shadcn/ui
- NextAuth.js (beta) for OAuth2/OIDC via Keycloak
- Axios for API calls
- @tanstack/react-query for data fetching

## Architecture
- Port: 3000
- API Gateway: http://localhost:8888
- Keycloak: http://localhost:8080/realms/iam-realm

## Pages
- /                → Landing page with Login button
- /dashboard       → Protected: current user profile
- /admin           → Protected: admin only, list of all users

## Auth Flow
1. User clicks Login → redirected to Keycloak
2. Keycloak authenticates → redirects back with code
3. NextAuth exchanges code for JWT token
4. JWT stored in session
5. API calls include Bearer token in Authorization header

## Environment Variables
NEXTAUTH_URL=http://localhost:3000
NEXTAUTH_SECRET=supersecretkey
KEYCLOAK_CLIENT_ID=iam-client
KEYCLOAK_CLIENT_SECRET=(from Keycloak)
KEYCLOAK_ISSUER=http://localhost:8080/realms/iam-realm
NEXT_PUBLIC_API_URL=http://localhost:8888

## Code Style
- TypeScript strict mode
- Functional components only
- No class components
- Use server components where possible
- Client components marked with 'use client'
