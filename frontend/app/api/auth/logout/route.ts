export async function GET() {
  const issuerPublic =
    process.env.KEYCLOAK_ISSUER_PUBLIC ??
    process.env.KEYCLOAK_ISSUER ??
    "http://localhost:8080/realms/iam-realm";

  const keycloakLogoutUrl =
    `${issuerPublic}/protocol/openid-connect/logout` +
    `?client_id=${process.env.KEYCLOAK_CLIENT_ID}` +
    `&post_logout_redirect_uri=${encodeURIComponent(process.env.NEXTAUTH_URL ?? "http://localhost:3000")}`;

  return Response.redirect(keycloakLogoutUrl);
}
