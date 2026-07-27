import { useQuery } from "@tanstack/react-query";
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
import { enrollments, page } from "../../../test/fixtures";
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
              </tr>
            </thead>
            <tbody>
              {query.data.content.map((item) => (
                <tr key={item.id}>
                  <td>
                    <PrimaryCell
                      title={
                        item.semester.name
                      }
                      detail={`Registered ${new Date(item.createdAt).toLocaleDateString()}`}
                    />
                  </td>
                  <td>
                    {item.details.map((detail) => (
                      <Link
                        className={styles.link}
                        key={detail.id}
                        to={`/student/sections/${detail.sectionId}`}
                      >
                        {detail.subject.code} · {detail.subject.name} · {detail.section.sectionCode}
                      </Link>
                    ))}
                  </td>
                  <td>{item.totalCredits}</td>
                  <td>
                    <StatusBadge value={item.status} />
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
