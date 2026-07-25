import type { ReactNode } from "react";
import type { RouteObject } from "react-router-dom";
import { RequireRole } from "../auth/RequireRole";
import { AdminOverviewPage } from "../features/admin/AdminOverviewPage";
import { CatalogPage } from "../features/admin/catalog/CatalogPage";
import { PeoplePage } from "../features/admin/people/PeoplePage";
import { AdminEnrollmentsPage } from "../features/admin/enrollments/AdminEnrollmentsPage";
const admin = (element: ReactNode) => (
  <RequireRole role="ADMIN">{element}</RequireRole>
);
export const adminRoutes: RouteObject[] = [
  { path: "admin/overview", element: admin(<AdminOverviewPage />) },
  { path: "admin/academic/:kind", element: admin(<CatalogPage />) },
  { path: "admin/people/:kind", element: admin(<PeoplePage />) },
  { path: "admin/enrollments", element: admin(<AdminEnrollmentsPage />) },
];
