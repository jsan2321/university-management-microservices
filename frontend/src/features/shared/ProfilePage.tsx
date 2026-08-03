import { useQuery } from "@tanstack/react-query";
import { useAuth } from "../../auth/AuthProvider";
import { useServiceApi } from "../../api/use-service-api";
import {
  ErrorState,
  LoadingState,
  PageHeader,
  Panel,
  StatusBadge,
} from "../../components/ui";
import styles from "../feature.module.css";
export function ProfilePage() {
  const { session } = useAuth();
  const api = useServiceApi();
  const query = useQuery({
    queryKey: [session?.role, "profile"],
    queryFn: () => {
      if (session?.role === "ADMIN") {
        const displayName = session.name.replace("Local ", "");
        const parts = displayName.split(" ");
        return Promise.resolve({
          firstName: parts[0] || "System",
          lastName: parts.slice(1).join(" "),
          email: session.email || "admin@ums.local",
          status: "ACTIVE",
          adminCode: `ADM-${session.username.toUpperCase()}`,
          hireDate: "System Default",
          phone: "Not required",
        });
      }
      return session?.role === "TEACHER"
          ? api.teacherMe()
          : api.studentMe();
    },
  });
  if (query.isPending) return <LoadingState />;
  if (query.error)
    return (
      <ErrorState error={query.error} retry={() => void query.refetch()} />
    );
  const profile = query.data;
  const code =
    "teacherCode" in profile
      ? profile.teacherCode
      : "adminCode" in profile
      ? profile.adminCode
      : profile.studentCode;
  return (
    <>
      <PageHeader
        eyebrow="Account-linked profile"
        title="My profile"
        description="View your personal information and university credentials."
      />
      <div className={styles.identity}>
        <Panel>
          <div className={styles.identityCard}>
            <span className={styles.identityAvatar}>
              {profile.firstName[0]}
              {profile.lastName[0]}
            </span>
            <h2>
              {profile.firstName} {profile.lastName}
            </h2>
            <p>{session?.username}</p>
            <StatusBadge value={profile.status} />
          </div>
        </Panel>
        <Panel
          title="Academic details"
          description="Read-only information from your profile service"
        >
          <dl className={styles.definition}>
            <div>
              <dt>Email</dt>
              <dd>{profile.email}</dd>
            </div>
            <div>
              <dt>
                {"teacherCode" in profile
                  ? "Teacher code"
                  : "adminCode" in profile
                  ? "Admin code"
                  : "Student code"}
              </dt>
              <dd>{code}</dd>
            </div>
            <div>
              <dt>
                {"teacherCode" in profile
                  ? "Hire date"
                  : "adminCode" in profile
                  ? "Account created"
                  : "Admission date"}
              </dt>
              <dd>
                {"teacherCode" in profile
                  ? profile.hireDate
                  : "adminCode" in profile
                  ? profile.hireDate
                  : profile.admissionDate}
              </dd>
            </div>
            <div>
              <dt>Phone</dt>
              <dd>{profile.phone || "Not provided"}</dd>
            </div>
          </dl>
        </Panel>
      </div>
    </>
  );
}
