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
  demoEnabled,
  demoSession,
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
  setDemoRole: (role: Role) => void;
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
        if (demoEnabled) setSession(demoSession("ADMIN"));
        else void login();
      },
      logout: () => {
        if (session?.demo) setSession(null);
        else void logout();
      },
      getToken,
      setDemoRole: (role) => {
        if (demoEnabled) setSession(demoSession(role));
      },
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
