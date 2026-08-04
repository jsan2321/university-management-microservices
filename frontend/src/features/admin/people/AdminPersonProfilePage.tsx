/* eslint-disable @typescript-eslint/no-explicit-any */
import { useQuery } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";
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
import uiStyles from "../../../components/ui.module.css";

export function AdminPersonProfilePage() {
  const { kind, id } = useParams();
  const api = useServiceApi();
  const isTeacher = kind === "teachers";

  const query = useQuery({
    queryKey: ["person-profile", kind, id],
    queryFn: async () => {
      if (isTeacher) {
        const profile = await api.teacher(id!);
        const depts = await api.departments(0, 100);
        const departmentName = depts.content.find(d => d.id === profile.departmentId)?.name;
        return { ...profile, departmentName };
      } else {
        const profile = await api.student(id!);
        const progs = await api.programs(0, 100);
        const program = progs.content.find(p => p.id === profile.programId);
        let departmentName;
        if (program) {
            const depts = await api.departments(0, 100);
            departmentName = depts.content.find(d => d.id === program.departmentId)?.name;
        }
        return { ...profile, programName: program?.name, departmentName };
      }
    },
    enabled: !!id,
  });

  const relatedQuery = useQuery({
    queryKey: ["person-related", kind, id],
    queryFn: async () => {
      if (isTeacher) {
        const res = await api.sections(0, 100, { teacherId: id });
        return res.content;
      } else {
        const res = await api.enrollments(0, 100, { studentId: id });
        return res.content;
      }
    },
    enabled: !!id,
  });

  if (query.isPending) return <LoadingState />;
  if (query.error)
    return (
      <ErrorState error={query.error} retry={() => void query.refetch()} />
    );

  const profile = query.data;
  const code =
    "teacherCode" in profile ? profile.teacherCode : profile.studentCode;

  const backLink = (
    <Link
      to={`/admin/people/${kind}`}
      className={uiStyles.button}
      style={{
        display: "inline-flex",
        alignItems: "center",
        gap: "0.5rem",
        padding: "0.25rem 0.5rem",
        marginBottom: "1rem",
        textDecoration: "none",
        color: "var(--ink-600)",
        background: "transparent",
        border: "none",
      }}
    >
      <ChevronLeft size={16} />
      Back to {isTeacher ? "teachers" : "students"}
    </Link>
  );

  return (
    <>
      <PageHeader
        eyebrow={backLink}
        title={`${profile.firstName} ${profile.lastName}`}
        description="Detailed academic profile."
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
            <StatusBadge value={profile.status} />
          </div>
        </Panel>
        <Panel
          title="Academic details"
          description={`Read-only information from the ${isTeacher ? "teacher" : "student"} service.`}
        >
          <dl className={styles.definition}>
            <div>
              <dt>Email</dt>
              <dd>{profile.email}</dd>
            </div>
            <div>
              <dt>{isTeacher ? "Teacher code" : "Student code"}</dt>
              <dd>{code}</dd>
            </div>
            <div>
              <dt>{isTeacher ? "Hire date" : "Admission date"}</dt>
              <dd>
                {isTeacher
                  ? profile.hireDate
                  : profile.admissionDate}
              </dd>
            </div>
            <div>
              <dt>Phone</dt>
              <dd>{profile.phone || "Not provided"}</dd>
            </div>
            {"departmentName" in profile && profile.departmentName && (
              <div>
                <dt>Department</dt>
                <dd>{profile.departmentName}</dd>
              </div>
            )}
            {"programName" in profile && profile.programName && (
              <div>
                <dt>Program</dt>
                <dd>{profile.programName}</dd>
              </div>
            )}
            {!isTeacher && "gender" in profile && (
              <div>
                <dt>Gender</dt>
                <dd>{profile.gender || "Not provided"}</dd>
              </div>
            )}
            {!isTeacher && "dateOfBirth" in profile && (
              <div>
                <dt>Date of birth</dt>
                <dd>{profile.dateOfBirth}</dd>
              </div>
            )}
          </dl>
        </Panel>
      </div>

      <div style={{ marginTop: "2rem" }}>
        <Panel
          title={isTeacher ? "Assigned Sections" : "Enrollments"}
          description={isTeacher ? "Sections this teacher is currently assigned to." : "Student's enrollment history."}
        >
          {relatedQuery.isPending ? (
            <LoadingState label="Loading academic history..." />
          ) : relatedQuery.error ? (
            <ErrorState error={relatedQuery.error} retry={() => void relatedQuery.refetch()} />
          ) : relatedQuery.data.length === 0 ? (
            <p style={{ color: "var(--ink-500)", padding: "1rem" }}>No records found.</p>
          ) : (
            <ul style={{ listStyle: "none", padding: "0" }}>
              {isTeacher
                ? relatedQuery.data.map((section: any) => (
                    <li key={section.id} style={{ padding: "0.75rem", borderBottom: "1px solid var(--ink-200)" }}>
                      <strong>{section.sectionCode}</strong> — {section.capacity} capacity <StatusBadge value={section.status} />
                    </li>
                  ))
                : relatedQuery.data.map((enrollment: any) => (
                    <li key={enrollment.id} style={{ padding: "0.75rem", borderBottom: "1px solid var(--ink-200)" }}>
                      <strong>{enrollment.semester.name}</strong> — {enrollment.totalCredits} credits <StatusBadge value={enrollment.status} />
                      <div style={{ fontSize: "0.85rem", color: "var(--ink-600)", marginTop: "4px" }}>
                        {enrollment.details.map((d: any) => d.subject.name).join(", ")}
                      </div>
                    </li>
                  ))}
            </ul>
          )}
        </Panel>
      </div>
    </>
  );
}
