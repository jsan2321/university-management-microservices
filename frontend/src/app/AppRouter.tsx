import { Navigate, useRoutes, type RouteObject } from "react-router-dom";
import { AppShell } from "./AppShell";
import { useAuth } from "../auth/AuthProvider";
import {
  LandingPage,
  NotFoundPage,
  UnauthorizedPage,
} from "../routes/SystemPages";
import { adminRoutes } from "../routes/admin.routes";
import { teacherRoutes } from "../routes/teacher.routes";
import { studentRoutes } from "../routes/student.routes";
function ProtectedShell() {
  const { session } = useAuth();
  return session ? <AppShell /> : <Navigate to="/" replace />;
}
const routes: RouteObject[] = [
  { path: "/", element: <LandingPage /> },
  { path: "/unauthorized", element: <UnauthorizedPage /> },
  {
    element: <ProtectedShell />,
    children: [...adminRoutes, ...teacherRoutes, ...studentRoutes],
  },
  { path: "*", element: <NotFoundPage /> },
];
export function AppRouter() {
  return useRoutes(routes);
}
