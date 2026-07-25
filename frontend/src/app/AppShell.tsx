import { useState } from "react";
import {
  BookOpen,
  CalendarCheck,
  GraduationCap,
  LayoutDashboard,
  Library,
  LogOut,
  Menu,
  NotebookPen,
  UserRound,
  UsersRound,
  X,
} from "lucide-react";
import { NavLink, Outlet, useNavigate } from "react-router-dom";
import type { Role } from "../api/generated/contracts";
import { useAuth } from "../auth/AuthProvider";
import { roleLabels } from "../auth/session";
import { Button } from "../components/ui";
import styles from "./AppShell.module.css";

const navigation: Record<
  Role,
  { label: string; to: string; icon: typeof LayoutDashboard }[]
> = {
  ADMIN: [
    { label: "Overview", to: "/admin/overview", icon: LayoutDashboard },
    {
      label: "Academic catalog",
      to: "/admin/academic/departments",
      icon: Library,
    },
    { label: "Students", to: "/admin/people/students", icon: GraduationCap },
    { label: "Teachers", to: "/admin/people/teachers", icon: UsersRound },
    { label: "Enrollments", to: "/admin/enrollments", icon: BookOpen },
  ],
  TEACHER: [
    { label: "Overview", to: "/teacher/overview", icon: LayoutDashboard },
    { label: "My sections", to: "/teacher/sections", icon: BookOpen },
    { label: "Assignments", to: "/teacher/assignments", icon: NotebookPen },
    { label: "My profile", to: "/teacher/profile", icon: UserRound },
  ],
  STUDENT: [
    { label: "Overview", to: "/student/overview", icon: LayoutDashboard },
    { label: "My enrollments", to: "/student/enrollments", icon: BookOpen },
    { label: "Assignments", to: "/student/assignments", icon: NotebookPen },
    { label: "My profile", to: "/student/profile", icon: UserRound },
  ],
};
export function Brand() {
  return (
    <div className={styles.brand}>
      <span className={styles.mark}>
        <GraduationCap size={21} />
      </span>
      <div>
        <strong>University Portal</strong>
        <span>Academic portal</span>
      </div>
    </div>
  );
}
function initials(name: string) {
  return name
    .split(/\s+/)
    .slice(0, 2)
    .map((part) => part[0])
    .join("")
    .toUpperCase();
}
export function AppShell() {
  const { session, logout, setDemoRole } = useAuth();
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();
  if (!session) return null;
  return (
    <div className={styles.shell}>
      <aside className={`${styles.sidebar} ${open ? styles.open : ""}`}>
        <button
          className={styles.close}
          onClick={() => setOpen(false)}
          aria-label="Close navigation"
        >
          <X />
        </button>
        <Brand />
        <section className={styles.term}>
          <div className={styles.termTop}>
            <span>Current term</span>
            <span>Week 9</span>
          </div>
          <strong>2026 · Fall semester</strong>
          <div className={styles.termTrack}>
            <span />
          </div>
        </section>
        <nav className={styles.nav} aria-label="Primary navigation">
          <p className={styles.navLabel}>
            {roleLabels[session.role]} workspace
          </p>
          {navigation[session.role].map(({ label, to, icon: Icon }) => (
            <NavLink
              key={to}
              to={to}
              onClick={() => setOpen(false)}
              className={({ isActive }) => (isActive ? styles.active : "")}
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </nav>
        <div className={styles.sidebarFoot}>
          <Button variant="secondary" onClick={logout}>
            <LogOut size={17} />
            Sign out
          </Button>
        </div>
      </aside>
      {open && (
        <button
          className={styles.scrim}
          onClick={() => setOpen(false)}
          aria-label="Close navigation overlay"
        />
      )}
      <main className={styles.main}>
        <header className={styles.topbar}>
          <button
            className={styles.menu}
            onClick={() => setOpen(true)}
            aria-label="Open navigation"
          >
            <Menu />
          </button>
          <div className={styles.context}>
            <span>University operations</span>
            <strong>{roleLabels[session.role]} workspace</strong>
          </div>
          <div className={styles.profile}>
            <span className={styles.avatar}>{initials(session.name)}</span>
            <div className={styles.user}>
              <strong>{session.name}</strong>
              <span>{session.username}</span>
            </div>
          </div>
        </header>
        {session.demo && (
          <div className={styles.demo}>
            <CalendarCheck size={14} />
            Preview data ·{" "}
            <select
              value={session.role}
              onChange={(event) => {
                const role = event.target.value as Role;
                setDemoRole(role);
                navigate(`/${role.toLowerCase()}/overview`);
              }}
              aria-label="Preview role"
            >
              <option value="ADMIN">Administrator</option>
              <option value="TEACHER">Teacher</option>
              <option value="STUDENT">Student</option>
            </select>
          </div>
        )}
        <div className={styles.content}>
          <Outlet />
        </div>
      </main>
    </div>
  );
}
