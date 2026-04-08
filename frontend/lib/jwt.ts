export function parseJwt(token: string): Record<string, unknown> | null {
  try {
    return JSON.parse(atob(token.split(".")[1]));
  } catch {
    return null;
  }
}

export function getRoles(accessToken?: string): string[] {
  if (!accessToken) return [];
  const payload = parseJwt(accessToken);
  if (!payload) return [];
  const realmAccess = payload.realm_access as { roles?: string[] } | undefined;
  return realmAccess?.roles ?? [];
}

export function hasRole(accessToken: string | undefined, role: string): boolean {
  const roles = getRoles(accessToken);
  return roles.includes(role) || roles.includes(`ROLE_${role}`);
}
