import type { ReactNode } from "react";
import type { RouteObject } from "react-router-dom";
import { RequireRole } from "../auth/RequireRole";
import { AdminOverviewPage } from "../features/admin/AdminOverviewPage";
import { CatalogPage } from "../features/admin/catalog/CatalogPage";
import { PeoplePage } from "../features/admin/people/PeoplePage";
import { AdminEnrollmentsPage } from "../features/admin/enrollments/AdminEnrollmentsPage";
import { AdminPersonProfilePage } from "../features/admin/people/AdminPersonProfilePage";
import { AdminEnrollmentDetailsPage } from "../features/admin/enrollments/AdminEnrollmentDetailsPage";
import { AdminAuditLogsPage } from "../features/admin/audit/AdminAuditLogsPage";
import { ProfilePage } from "../features/shared/ProfilePage";
const admin = (element: ReactNode) => (
  <RequireRole role="ADMIN">{element}</RequireRole>
);
export const adminRoutes: RouteObject[] = [
  { path: "admin/overview", element: admin(<AdminOverviewPage />) },
  { path: "admin/academic/:kind", element: admin(<CatalogPage />) },
  { path: "admin/people/:kind", element: admin(<PeoplePage />) },
  { path: "admin/people/:kind/:id", element: admin(<AdminPersonProfilePage />) },
  { path: "admin/enrollments", element: admin(<AdminEnrollmentsPage />) },
  { path: "admin/enrollments/:id", element: admin(<AdminEnrollmentDetailsPage />) },
  { path: "admin/audit-logs", element: admin(<AdminAuditLogsPage />) },
  { path: "admin/profile", element: admin(<ProfilePage />) },
];
