import NextAuth from "next-auth";
import Keycloak from "next-auth/providers/keycloak";

export const { handlers, auth, signIn, signOut } = NextAuth({
  trustHost: true,
  providers: [
    Keycloak({
      clientId: process.env.KEYCLOAK_CLIENT_ID!,
      clientSecret: process.env.KEYCLOAK_CLIENT_SECRET!,
      // Must match the `iss` claim Keycloak puts in tokens (public URL)
      issuer: process.env.KEYCLOAK_ISSUER_PUBLIC!,
      authorization: {
        url: `${process.env.KEYCLOAK_ISSUER_PUBLIC}/protocol/openid-connect/auth`,
        params: { scope: "openid profile email" },
      },
      // Server-side calls go through the internal Docker hostname
      token: `${process.env.KEYCLOAK_ISSUER}/protocol/openid-connect/token`,
      userinfo: `${process.env.KEYCLOAK_ISSUER}/protocol/openid-connect/userinfo`,
      jwks_endpoint: `${process.env.KEYCLOAK_ISSUER}/protocol/openid-connect/certs`,
    }),
  ],
  callbacks: {
    async jwt({ token, account }) {
      if (account) {
        token.accessToken = account.access_token;
        token.refreshToken = account.refresh_token;
        token.expiresAt = account.expires_at;
      }
      return token;
    },
    async session({ session, token }) {
      session.accessToken = token.accessToken as string;
      session.error = token.error as string | undefined;
      return session;
    },
  },
  events: {
    async signOut() {
      // handled by redirect below
    },
  },
  pages: {
    signIn: "/",
    signOut: "/",
  },
});
