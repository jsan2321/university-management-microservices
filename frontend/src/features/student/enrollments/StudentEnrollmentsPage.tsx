import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { ArrowRight } from "lucide-react";
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
import { enrollments, page, semesters } from "../../../test/fixtures";
import styles from "../../feature.module.css";
export function StudentEnrollmentsPage() {
  const api = useServiceApi();
  const { session } = useAuth();
  const query = useQuery({
    queryKey: ["student", "enrollments"],
    queryFn: () =>
      session?.demo ? page(enrollments) : api.myEnrollments(0, 100),
  });
  return (
    <>
      <PageHeader
        eyebrow="Academic registration"
        title="My enrollments"
        description="Your current and previous semester registrations, scoped to your signed-in account."
      />
      {query.isPending ? (
        <LoadingState />
      ) : query.error ? (
        <ErrorState error={query.error} retry={() => void query.refetch()} />
      ) : query.data.content.length === 0 ? (
        <EmptyState
          title="No enrollments found"
          description="When registration is complete, your semester and sections will appear here."
        />
      ) : (
        <Panel
          title="Enrollment history"
          description={`${query.data.totalElements} records`}
        >
          <DataTable>
            <thead>
              <tr>
                <th>Semester</th>
                <th>Sections</th>
                <th>Credits</th>
                <th>Status</th>
                <th>
                  <span className="sr-only">Open</span>
                </th>
              </tr>
            </thead>
            <tbody>
              {query.data.content.map((item) => (
                <tr key={item.id}>
                  <td>
                    <PrimaryCell
                      title={
                        semesters.find(
                          (semester) => semester.id === item.semesterId,
                        )?.name ?? item.semesterId
                      }
                      detail={`Registered ${new Date(item.createdAt).toLocaleDateString()}`}
                    />
                  </td>
                  <td>{item.details.length}</td>
                  <td>{item.totalCredits}</td>
                  <td>
                    <StatusBadge value={item.status} />
                  </td>
                  <td>
                    {item.details[0] && (
                      <Link
                        className={styles.link}
                        to={`/student/sections/${item.details[0].sectionId}`}
                      >
                        Open section <ArrowRight size={15} />
                      </Link>
                    )}
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
