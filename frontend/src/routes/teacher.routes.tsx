import type { ReactNode } from "react";
import type { RouteObject } from "react-router-dom";
import { RequireRole } from "../auth/RequireRole";
import { TeacherOverviewPage } from "../features/teacher/TeacherOverviewPage";
import { TeacherSectionsPage } from "../features/teacher/sections/TeacherSectionsPage";
import { AttendancePage } from "../features/teacher/attendance/AttendancePage";
import { AssignmentsPage } from "../features/shared/AssignmentsPage";
import { ProfilePage } from "../features/shared/ProfilePage";
const teacher = (element: ReactNode) => (
  <RequireRole role="TEACHER">{element}</RequireRole>
);
export const teacherRoutes: RouteObject[] = [
  { path: "teacher/overview", element: teacher(<TeacherOverviewPage />) },
  { path: "teacher/sections", element: teacher(<TeacherSectionsPage />) },
  {
    path: "teacher/sections/:sectionId/attendance",
    element: teacher(<AttendancePage />),
  },
  { path: "teacher/assignments", element: teacher(<AssignmentsPage />) },
  { path: "teacher/profile", element: teacher(<ProfilePage />) },
];
