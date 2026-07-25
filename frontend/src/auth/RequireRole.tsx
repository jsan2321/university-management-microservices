import type { ReactNode } from "react";
import { Navigate } from "react-router-dom";
import type { Role } from "../api/generated/contracts";
import { useAuth } from "./AuthProvider";
export function RequireRole({
  role,
  children,
}: {
  role: Role;
  children: ReactNode;
}) {
  const { session } = useAuth();
  if (!session) return <Navigate to="/" replace />;
  return session.role === role ? (
    children
  ) : (
    <Navigate to="/unauthorized" replace />
  );
}
