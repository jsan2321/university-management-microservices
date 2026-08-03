import type { Role } from "../api/generated/contracts";
export interface Session {
  name: string;
  username: string;
  email: string;
  role: Role;
  demo: boolean;
}
export function roleFromClaims(roles: string[] = []): Role | null {
  if (roles.includes("ADMIN")) return "ADMIN";
  if (roles.includes("TEACHER")) return "TEACHER";
  if (roles.includes("STUDENT")) return "STUDENT";
  return null;
}
export const roleLabels: Record<Role, string> = {
  ADMIN: "Administrator",
  TEACHER: "Teacher",
  STUDENT: "Student",
};
export const roleHome: Record<Role, string> = {
  ADMIN: "/admin/overview",
  TEACHER: "/teacher/overview",
  STUDENT: "/student/overview",
};
