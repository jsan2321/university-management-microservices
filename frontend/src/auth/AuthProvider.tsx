import {
  createContext,
  useCallback,
  useContext,
  useMemo,
  useState,
  type ReactNode,
} from "react";
import type { Role } from "../api/generated/contracts";
import {
  freshToken,
  login,
  logout,
} from "./keycloak";
import type { Session } from "./session";

interface AuthValue {
  session: Session | null;
  login: () => void;
  logout: () => void;
  getToken: () => Promise<string>;
}
const AuthContext = createContext<AuthValue | null>(null);
export function AuthProvider({
  initialSession,
  children,
}: {
  initialSession: Session | null;
  children: ReactNode;
}) {
  const [session, setSession] = useState(initialSession);
  const getToken = useCallback(() => freshToken(session), [session]);
  const value = useMemo<AuthValue>(
    () => ({
      session,
      login: () => {
        void login();
      },
      logout: () => {
        void logout();
      },
      getToken,
    }),
    [getToken, session],
  );
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
export function useAuth() {
  const value = useContext(AuthContext);
  if (!value) throw new Error("useAuth must be used inside AuthProvider");
  return value;
}
