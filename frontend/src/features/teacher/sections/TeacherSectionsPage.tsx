import { useQuery } from "@tanstack/react-query";
import { ArrowRight } from "lucide-react";
import { Link } from "react-router-dom";
import { useServiceApi } from "../../../api/use-service-api";
import { useAuth } from "../../../auth/AuthProvider";
import {
  DataTable,
  EmptyState,
  ErrorState,
  LoadingState,
  PageHeader,
  Panel,
  PrimaryCell,
  StatusBadge,
} from "../../../components/ui";
import { teacherSections } from "../../../test/fixtures";
import { teacherSectionLabel } from "../teacher-section";
import styles from "../../feature.module.css";
export function TeacherSectionsPage() {
  const api = useServiceApi();
  const { session } = useAuth();
  const query = useQuery({
    queryKey: ["teacher", "sections"],
    queryFn: async () => session?.demo ? teacherSections : api.teacherSections(),
  });
  return (
    <>
      <PageHeader
        eyebrow="Teaching assignments"
        title="My sections"
        description="Each section is the working context for attendance and assignments."
      />
      {query.isPending ? (
        <LoadingState />
      ) : query.error ? (
        <ErrorState error={query.error} retry={() => void query.refetch()} />
      ) : query.data.length === 0 ? (
        <EmptyState
          title="No sections assigned"
          description="Sections assigned to your teacher profile will appear here."
        />
      ) : (
        <Panel
          title="Assigned sections"
          description={`${query.data.length} sections`}
        >
          <DataTable>
            <thead>
              <tr>
                <th>Section</th>
                <th>Schedule</th>
                <th>Capacity</th>
                <th>Status</th>
                <th>
                  <span className="sr-only">Actions</span>
                </th>
              </tr>
            </thead>
            <tbody>
              {query.data.map((section) => (
                <tr key={section.id}>
                  <td>
                    <PrimaryCell
                      title={teacherSectionLabel(section)}
                      detail={section.semester.name}
                    />
                  </td>
                  <td>
                    {section.schedules
                      .map(
                        (schedule) =>
                          `${schedule.dayOfWeek.slice(0, 3)} ${schedule.startTime}–${schedule.endTime}`,
                      )
                      .join(" · ")}
                  </td>
                  <td>{section.capacity}</td>
                  <td>
                    <StatusBadge value={section.status} />
                  </td>
                  <td>
                    <Link
                      className={styles.link}
                      to={`/teacher/sections/${section.id}/attendance`}
                    >
                      Record attendance <ArrowRight size={15} />
                    </Link>
                  </td>
                </tr>
              ))}
            </tbody>
          </DataTable>
        </Panel>
      )}
    </>
  );
}
