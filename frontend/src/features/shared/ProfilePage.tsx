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
import { students, teachers } from "../../test/fixtures";
import styles from "../feature.module.css";
export function ProfilePage() {
  const { session } = useAuth();
  const api = useServiceApi();
  const query = useQuery({
    queryKey: [session?.role, "profile"],
    queryFn: () =>
      session?.demo
        ? session.role === "TEACHER"
          ? teachers[0]
          : students[0]
        : session?.role === "TEACHER"
          ? api.teacherMe()
          : api.studentMe(),
  });
  if (query.isPending) return <LoadingState />;
  if (query.error)
    return (
      <ErrorState error={query.error} retry={() => void query.refetch()} />
    );
  const profile = query.data;
  const code =
    "teacherCode" in profile ? profile.teacherCode : profile.studentCode;
  return (
    <>
      <PageHeader
        eyebrow="Account-linked profile"
        title="My profile"
        description="This academic profile is linked to your Keycloak identity. You never need to enter or copy a user ID."
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
            <p>{code}</p>
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
              <dt>Profile code</dt>
              <dd>{code}</dd>
            </div>
            <div>
              <dt>
                {"teacherCode" in profile ? "Hire date" : "Admission date"}
              </dt>
              <dd>
                {"teacherCode" in profile
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
