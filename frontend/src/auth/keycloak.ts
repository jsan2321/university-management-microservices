import Keycloak from "keycloak-js";
import { roleFromClaims, type Session } from "./session";
import type { Role } from "../api/generated/contracts";

// Demo mode is only available during local development (import.meta.env.DEV is
// always false in production builds — tree-shaken out at compile time).
// Setting VITE_DEMO_MODE=true in a production .env has no effect.
const demoEnabled = import.meta.env.DEV && import.meta.env.VITE_DEMO_MODE === "true";
const demoNames: Record<Role, string> = {
  ADMIN: "Elena Morales",
  TEACHER: "Marcus Lee",
  STUDENT: "Sofia Chen",
};
let keycloak: Keycloak | undefined;
function adapter() {
  keycloak ??= new Keycloak({
    url: import.meta.env.VITE_KEYCLOAK_URL ?? "http://localhost:8180",
    realm: import.meta.env.VITE_KEYCLOAK_REALM ?? "ums",
    clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID ?? "ums-web",
  });
  return keycloak;
}
export async function bootstrapSession(): Promise<Session | null> {
  if (demoEnabled) return demoSession("ADMIN");
  const kc = adapter();
  const authenticated = await kc.init({
    onLoad: "check-sso",
    pkceMethod: "S256",
    checkLoginIframe: false,
  });
  if (!authenticated || !kc.token) return null;
  const role = roleFromClaims(kc.realmAccess?.roles);
  if (!role) return null;
  return {
    name: String(
      kc.tokenParsed?.name ??
        kc.tokenParsed?.preferred_username ??
        "University user",
    ),
    username: String(kc.tokenParsed?.preferred_username ?? "user"),
    email: String(kc.tokenParsed?.email ?? ""),
    role,
    demo: false,
  };
}
export function demoSession(role: Role): Session {
  return {
    name: demoNames[role],
    username: `demo.${role.toLowerCase()}`,
    email: `demo.${role.toLowerCase()}@example.com`,
    role,
    demo: true,
  };
}
export function login() {
  return adapter().login();
}
export function logout() {
  return adapter().logout({ redirectUri: window.location.origin });
}
export async function freshToken(session: Session | null) {
  if (session?.demo) return "demo";
  const kc = adapter();
  try {
    await kc.updateToken(30);
  } catch {
    await logout();
    throw new Error("Your session expired. Please sign in again.");
  }
  if (!kc.token) throw new Error("Authentication session is unavailable.");
  return kc.token;
}
export { demoEnabled };
