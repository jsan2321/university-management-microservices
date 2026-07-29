import type { ReactNode } from "react";
import type { RouteObject } from "react-router-dom";
import { RequireRole } from "../auth/RequireRole";
import { StudentOverviewPage } from "../features/student/StudentOverviewPage";
import { StudentEnrollmentsPage } from "../features/student/enrollments/StudentEnrollmentsPage";
import { StudentEnrollmentDetailsPage } from "../features/student/enrollments/StudentEnrollmentDetailsPage";
import { StudentClassesPage } from "../features/student/classes/StudentClassesPage";
import { StudentSectionPage } from "../features/student/sections/StudentSectionPage";
import { AssignmentsPage } from "../features/shared/AssignmentsPage";
import { ProfilePage } from "../features/shared/ProfilePage";
const student = (element: ReactNode) => (
  <RequireRole role="STUDENT">{element}</RequireRole>
);
export const studentRoutes: RouteObject[] = [
  { path: "student/overview", element: student(<StudentOverviewPage />) },
  { path: "student/classes", element: student(<StudentClassesPage />) },
  { path: "student/enrollments", element: student(<StudentEnrollmentsPage />) },
  { path: "student/enrollments/:id", element: student(<StudentEnrollmentDetailsPage />) },
  {
    path: "student/classes/:sectionId",
    element: student(<StudentSectionPage />),
  },
  { path: "student/assignments", element: student(<AssignmentsPage />) },
  { path: "student/profile", element: student(<ProfilePage />) },
];
