import { useQuery } from "@tanstack/react-query";
import { useParams, useNavigate } from "react-router-dom";
import { ChevronLeft } from "lucide-react";
import { useServiceApi } from "../../../api/use-service-api";
import {
  ErrorState,
  LoadingState,
  PageHeader,
  Panel,
  StatusBadge,
} from "../../../components/ui";
import styles from "../../feature.module.css";
export function StudentSectionPage() {
  const { sectionId = "" } = useParams();
  const navigate = useNavigate();
  const api = useServiceApi();
  const query = useQuery({
    queryKey: ["student", "section", sectionId],
    enabled: Boolean(sectionId),
    queryFn: async () => {
      const [section, attendance, work, records] = await Promise.all([
        api.section(sectionId),
        api.myAttendance(sectionId),
        api.assignments(sectionId, 0, 100, "PUBLISHED", true),
        api.myEnrollments(0, 100),
      ]);
      const enrollment = records.content
        .flatMap((item) => item.details)
        .find((detail) => detail.sectionId === sectionId);
      if (!enrollment) throw new Error("This section is not in your active enrollment.");
      return { section, enrollment, attendance, assignments: work.content };
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
        eyebrow={
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <button 
              onClick={() => navigate(-1)} 
              style={{ display: 'inline-flex', alignItems: 'center', textDecoration: 'none', color: 'inherit', background: 'transparent', border: 'none', padding: 0, cursor: 'pointer', font: 'inherit' }}
            >
              <ChevronLeft size={16} /> Back
            </button>
            <span>/</span>
            <span>Class details</span>
          </div>
        }
        title={`${query.data.enrollment.subject.code} — ${query.data.enrollment.subject.name} · ${query.data.section.sectionCode}`}
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
                <strong>{item.dayOfWeek.charAt(0)}{item.dayOfWeek.slice(1).toLowerCase()}</strong>
                <span>
                  {formatTime(item.startTime)}–{formatTime(item.endTime)}
                </span>
              </div>
            </li>
          ))}
        </ul>
      </Panel>
    </>
  );
}

function formatTime(value: string) {
  const [hour, minute] = value.split(":").map(Number);
  return new Intl.DateTimeFormat(undefined, {
    hour: "numeric",
    minute: "2-digit",
  }).format(new Date(2000, 0, 1, hour, minute));
}
