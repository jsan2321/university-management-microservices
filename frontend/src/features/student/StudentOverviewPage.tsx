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
import styles from "../feature.module.css";
export function StudentOverviewPage() {
  const api = useServiceApi();
  const { session } = useAuth();
  const query = useQuery({
    queryKey: ["student", "overview"],
    queryFn: async () => {
      const profile = await api.studentMe();
      const records = await api.myEnrollments(0, 100, undefined, "ACTIVE");
      return { profile, enrollments: records.content };
    },
  });
  const upcomingQuery = useQuery({
    queryKey: ["student", "upcoming-work", query.data?.enrollments],
    enabled: Boolean(query.data),
    queryFn: async () => {
      const activeEnrollments = query.data!.enrollments;
      const contexts = activeEnrollments.flatMap((enrollment) =>
        enrollment.details.map((detail) => ({
          sectionId: detail.sectionId,
          label: `${detail.subject.code} — ${detail.subject.name} · ${detail.section.sectionCode}`,
        })),
      );
      const workBySection = await Promise.all(
            contexts.map(async (context) => ({
              context,
              content: (
                await api.assignments(
                  context.sectionId,
                  0,
                  100,
                  "PUBLISHED",
                  true,
                )
              ).content,
            })),
          );
      const now = new Date();
      return workBySection
        .flatMap(({ context, content }) =>
          content.map((assignment) => ({ ...assignment, sectionLabel: context.label })),
        )
        .filter((assignment) => new Date(assignment.dueAt) > now)
        .sort((left, right) =>
          new Date(left.dueAt).getTime() - new Date(right.dueAt).getTime(),
        );
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
        {upcomingQuery.isPending ? (
          <LoadingState label="Loading upcoming work…" />
        ) : upcomingQuery.error ? (
          <ErrorState
            error={upcomingQuery.error}
            retry={() => void upcomingQuery.refetch()}
          />
        ) : upcomingQuery.data?.length ? (
          <ul className={styles.taskList}>
            {upcomingQuery.data.map((item) => (
              <li key={item.id}>
                <div>
                  <strong>{item.title}</strong>
                  <span>{item.sectionLabel}</span>
                  <span>Due {new Date(item.dueAt).toLocaleString()}</span>
                </div>
                <Link
                  className={styles.link}
                  to={`/student/assignments?sectionId=${item.sectionId}`}
                >
                  View assignment <ArrowRight size={15} />
                </Link>
              </li>
            ))}
          </ul>
        ) : (
          <p>No upcoming published work.</p>
        )}
      </Panel>
    </>
  );
}
