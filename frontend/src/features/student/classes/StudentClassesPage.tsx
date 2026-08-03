import { useState } from "react";
import { useQuery } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { ArrowRight } from "lucide-react";
import { useServiceApi } from "../../../api/use-service-api";
import { useAuth } from "../../../auth/AuthProvider";
import {
  EmptyState,
  ErrorState,
  LoadingState,
  PageHeader,
  Panel,
  PrimaryCell,
  DataTable,
} from "../../../components/ui";
import styles from "../../feature.module.css";

export function StudentClassesPage() {
  const api = useServiceApi();
  const { session } = useAuth();
  const [selectedSemester, setSelectedSemester] = useState<string>("");

  const query = useQuery({
    queryKey: ["student", "enrollments-for-classes"],
    queryFn: () =>
      api.myEnrollments(0, 100),
  });

  if (query.isPending) return <LoadingState />;
  if (query.error)
    return <ErrorState error={query.error} retry={() => void query.refetch()} />;

  const activeEnrollments = query.data.content.filter(e => e.status === "ACTIVE");
  
  if (activeEnrollments.length === 0) {
    return (
      <>
        <PageHeader
          eyebrow="Academic portal"
          title="My classes"
          description="Your currently enrolled subjects."
        />
        <EmptyState
          title="No classes found"
          description="You are not currently enrolled in any active classes."
        />
      </>
    );
  }

  // Get unique semesters for the filter
  const semesters = Array.from(new Set(activeEnrollments.map(e => e.semesterId))).map(
    id => activeEnrollments.find(e => e.semesterId === id)!.semester
  );

  // Determine which semester to show
  const currentSemesterId = selectedSemester || semesters[0]?.id || "";
  const currentEnrollment = activeEnrollments.find(e => e.semesterId === currentSemesterId);

  return (
    <>
      <PageHeader
        eyebrow="Academic portal"
        title="My classes"
        description="Your currently enrolled subjects."
      />
      
      {semesters.length > 1 && (
        <div style={{ marginBottom: "20px" }}>
          <label style={{ marginRight: "10px", fontWeight: 500 }}>Select Semester:</label>
          <select 
            value={currentSemesterId} 
            onChange={(e) => setSelectedSemester(e.target.value)}
            style={{ padding: "6px", borderRadius: "4px", border: "1px solid #ccc" }}
          >
            {semesters.map(s => (
              <option key={s.id} value={s.id}>{s.name}</option>
            ))}
          </select>
        </div>
      )}

      {!currentEnrollment || currentEnrollment.details.length === 0 ? (
        <EmptyState
          title="No classes"
          description="You have no classes in this semester."
        />
      ) : (
        <Panel
          title={`${currentEnrollment.semester.name} Classes`}
          description={`${currentEnrollment.details.length} enrolled subjects`}
        >
          <DataTable>
            <thead>
              <tr>
                <th>Subject</th>
                <th>Section</th>
                <th>Credits</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {currentEnrollment.details.map((detail) => (
                <tr key={detail.id}>
                  <td>
                    <PrimaryCell
                      title={detail.subject.name}
                      detail={detail.subject.code}
                    />
                  </td>
                  <td>{detail.section.sectionCode}</td>
                  <td>{detail.credits}</td>
                  <td style={{ textAlign: "right" }}>
                    <Link
                      className={styles.link}
                      to={`/student/classes/${detail.sectionId}`}
                    >
                      View class <ArrowRight size={15} />
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
