import { useQuery } from "@tanstack/react-query";
import { useParams } from "react-router-dom";
import { useServiceApi } from "../../../api/use-service-api";
import { useAuth } from "../../../auth/AuthProvider";
import {
  ErrorState,
  LoadingState,
  PageHeader,
  Panel,
  StatusBadge,
} from "../../../components/ui";
import { assignments, sections } from "../../../test/fixtures";
import styles from "../../feature.module.css";
export function StudentSectionPage() {
  const { sectionId = "" } = useParams();
  const api = useServiceApi();
  const { session } = useAuth();
  const query = useQuery({
    queryKey: ["student", "section", sectionId],
    enabled: Boolean(sectionId),
    queryFn: async () => {
      if (session?.demo)
        return {
          section: sections.find((item) => item.id === sectionId)!,
          attendance: {
            percentage: 92,
            presentCount: 11,
            totalSessions: 12,
            eligibleForFinalEvaluation: true,
          },
          assignments,
        };
      const [section, attendance, work] = await Promise.all([
        api.section(sectionId),
        api.myAttendance(sectionId),
        api.assignments(sectionId, 0, 100, "PUBLISHED", true),
      ]);
      return { section, attendance, assignments: work.content };
    },
  });
  if (query.isPending) return <LoadingState />;
  if (query.error)
    return (
      <ErrorState error={query.error} retry={() => void query.refetch()} />
    );
  return (
    <>
      <PageHeader
        eyebrow="Enrolled section"
        title={query.data.section.sectionCode}
        description="Course context, attendance progress, and published work for this section."
      />
      <section className={styles.stats}>
        <div className={styles.stat}>
          <span>Attendance</span>
          <strong>{Math.round(query.data.attendance.percentage)}%</strong>
          <small>
            {query.data.attendance.presentCount} of{" "}
            {query.data.attendance.totalSessions} sessions
          </small>
        </div>
        <div className={styles.stat}>
          <span>Final evaluation</span>
          <strong style={{ fontSize: 23 }}>
            {query.data.attendance.eligibleForFinalEvaluation
              ? "Eligible"
              : "At risk"}
          </strong>
          <small>Based on attendance policy</small>
        </div>
        <div className={styles.stat}>
          <span>Published work</span>
          <strong>{query.data.assignments.length}</strong>
          <small>Visible assignments</small>
        </div>
        <div className={styles.stat}>
          <span>Section status</span>
          <StatusBadge value={query.data.section.status} />
          <small style={{ display: "block", marginTop: 12 }}>
            {query.data.section.capacity} seat capacity
          </small>
        </div>
      </section>
      <Panel
        title="Meeting schedule"
        description="Times published for this section"
      >
        <ul className={styles.taskList}>
          {query.data.section.schedules.map((item) => (
            <li key={`${item.dayOfWeek}-${item.startTime}`}>
              <div>
                <strong>{item.dayOfWeek}</strong>
                <span>
                  {item.startTime}–{item.endTime}
                </span>
              </div>
            </li>
          ))}
        </ul>
      </Panel>
    </>
  );
}
