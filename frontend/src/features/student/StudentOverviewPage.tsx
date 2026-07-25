import { useQuery } from "@tanstack/react-query";
import { ArrowRight } from "lucide-react";
import { Link } from "react-router-dom";
import { useServiceApi } from "../../api/use-service-api";
import { useAuth } from "../../auth/AuthProvider";
import {
  ErrorState,
  LoadingState,
  PageHeader,
  Panel,
} from "../../components/ui";
import { assignments, enrollments, page, students } from "../../test/fixtures";
import styles from "../feature.module.css";
export function StudentOverviewPage() {
  const api = useServiceApi();
  const { session } = useAuth();
  const query = useQuery({
    queryKey: ["student", "overview"],
    queryFn: async () => {
      const profile = session?.demo ? students[0] : await api.studentMe();
      const records = session?.demo
        ? page(enrollments)
        : await api.myEnrollments(0, 100, undefined, "ACTIVE");
      return { profile, enrollments: records.content };
    },
  });
  if (query.isPending) return <LoadingState />;
  if (query.error)
    return (
      <ErrorState error={query.error} retry={() => void query.refetch()} />
    );
  const credits = query.data.enrollments.reduce(
    (sum, item) => sum + item.totalCredits,
    0,
  );
  const courses = query.data.enrollments.reduce(
    (sum, item) => sum + item.details.length,
    0,
  );
  return (
    <>
      <PageHeader
        eyebrow="Student workspace"
        title={`Welcome, ${query.data.profile.firstName}.`}
        description="See what is active now, what is due next, and where your academic work stands."
      />
      <section className={styles.stats}>
        <div className={styles.stat}>
          <span>Active enrollments</span>
          <strong>{query.data.enrollments.length}</strong>
          <small>Current semester</small>
        </div>
        <div className={styles.stat}>
          <span>Registered sections</span>
          <strong>{courses}</strong>
          <small>Across active enrollments</small>
        </div>
        <div className={styles.stat}>
          <span>Registered credits</span>
          <strong>{credits}</strong>
          <small>Current academic load</small>
        </div>
        <div className={styles.stat}>
          <span>Student code</span>
          <strong style={{ fontSize: 25 }}>
            {query.data.profile.studentCode}
          </strong>
          <small>Academic profile</small>
        </div>
      </section>
      <Panel
        title="Upcoming work"
        description="Published assignments ordered by due date"
      >
        <ul className={styles.taskList}>
          {assignments.map((item) => (
            <li key={item.id}>
              <div>
                <strong>{item.title}</strong>
                <span>Due {new Date(item.dueAt).toLocaleString()}</span>
              </div>
              <Link className={styles.link} to="/student/assignments">
                View assignment <ArrowRight size={15} />
              </Link>
            </li>
          ))}
        </ul>
      </Panel>
    </>
  );
}
