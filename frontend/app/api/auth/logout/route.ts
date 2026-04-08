export async function GET() {
  const keycloakLogoutUrl =
    `${process.env.KEYCLOAK_ISSUER}/protocol/openid-connect/logout` +
    `?client_id=${process.env.KEYCLOAK_CLIENT_ID}` +
    `&post_logout_redirect_uri=${encodeURIComponent(process.env.NEXTAUTH_URL ?? "http://localhost:3000")}`;

  return Response.redirect(keycloakLogoutUrl);
}
