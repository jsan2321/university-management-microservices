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
  StatusBadge,
} from "../../components/ui";
import { teacherSections, teachers } from "../../test/fixtures";
import { teacherSectionLabel } from "./teacher-section";
import styles from "../feature.module.css";
export function TeacherOverviewPage() {
  const api = useServiceApi();
  const { session } = useAuth();
  const query = useQuery({
    queryKey: ["teacher", "overview"],
    queryFn: async () => {
      const teacher = session?.demo ? teachers[0] : await api.teacherMe();
      const assigned = session?.demo ? teacherSections : await api.teacherSections();
      return { teacher, sections: assigned.filter((section) => section.status === "ACTIVE") };
    },
  });
  const today = new Intl.DateTimeFormat("en-US", { weekday: "long" })
    .format(new Date())
    .toUpperCase();
  if (query.isPending) return <LoadingState />;
  if (query.error)
    return (
      <ErrorState error={query.error} retry={() => void query.refetch()} />
    );
  return (
    <>
      <PageHeader
        eyebrow="Teaching workspace"
        title={`Good morning, ${query.data.teacher.firstName}.`}
        description="Your assigned sections and the next academic tasks for this semester."
      />
      <section className={styles.stats}>
        <div className={styles.stat}>
          <span>Active sections</span>
          <strong>{query.data.sections.length}</strong>
          <small>Assigned to your profile</small>
        </div>
        <div className={styles.stat}>
          <span>Account</span>
          <StatusBadge value={query.data.teacher.status} />
          <small style={{ display: "block", marginTop: 13 }}>
            {query.data.teacher.email}
          </small>
        </div>
      </section>
      <Panel
        title="Today's classes"
        description="Open a scheduled class to record attendance."
      >
        <ul className={styles.taskList}>
          {query.data.sections
            .filter((section) =>
              section.schedules.some(
                (schedule) =>
                  schedule.dayOfWeek === today,
              ),
            )
            .map((section) => (
              <li key={section.id}>
                <div>
                  <strong>{teacherSectionLabel(section)}</strong>
                  <span>
                    {section.schedules
                      .filter(
                        (schedule) =>
                          schedule.dayOfWeek === today,
                      )
                      .map((schedule) => `${schedule.startTime}–${schedule.endTime}`)
                      .join(" · ")}
                  </span>
                </div>
                <Link className={styles.link} to={`/teacher/sections/${section.id}/attendance`}>
                  Record attendance <ArrowRight size={15} />
                </Link>
              </li>
            ))}
          {!query.data.sections.some((section) =>
            section.schedules.some(
              (schedule) =>
                schedule.dayOfWeek === today,
            ),
          ) && <li><span>No classes are scheduled for today.</span></li>}
        </ul>
      </Panel>
      <Panel
        title="Assigned sections"
        description="Open a section to manage its academic work"
      >
        <ul className={styles.taskList}>
          {query.data.sections.map((section) => (
            <li key={section.id}>
              <div>
                <strong>
                  {teacherSectionLabel(section)}
                </strong>
                <span>
                  {section.schedules
                    .map(
                      (item) =>
                        `${item.dayOfWeek.slice(0, 3)} ${item.startTime}`,
                    )
                    .join(" · ")}
                </span>
              </div>
              <Link
                className={styles.link}
                to={`/teacher/sections/${section.id}/attendance`}
              >
                Record attendance <ArrowRight size={15} />
              </Link>
            </li>
          ))}
        </ul>
      </Panel>
    </>
  );
}
