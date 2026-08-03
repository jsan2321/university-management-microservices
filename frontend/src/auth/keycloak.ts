import Keycloak from "keycloak-js";
import { roleFromClaims, type Session } from "./session";
import type { Role } from "../api/generated/contracts";

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

export function login() {
  return adapter().login();
}
export function logout() {
  return adapter().logout({ redirectUri: window.location.origin });
}
export async function freshToken(session: Session | null) {
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
