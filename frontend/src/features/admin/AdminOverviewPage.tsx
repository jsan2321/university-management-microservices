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
import { departments, sections, students, teachers } from "../../test/fixtures";
import styles from "../feature.module.css";
export function AdminOverviewPage() {
  const api = useServiceApi();
  const { session } = useAuth();
  const query = useQuery({
    queryKey: ["admin", "overview"],
    queryFn: async () => {
      if (session?.demo)
        return {
          students: students.length,
          teachers: teachers.length,
          departments: departments.length,
          sections: sections.length,
        };
      const [s, t, d, sec] = await Promise.all([
        api.students(0, 1),
        api.teachers(0, 1),
        api.departments(0, 1),
        api.sections(0, 1),
      ]);
      return {
        students: s.totalElements,
        teachers: t.totalElements,
        departments: d.totalElements,
        sections: sec.totalElements,
      };
    },
  });
  return (
    <>
      <PageHeader
        eyebrow="Institutional operations"
        title="University overview"
        description="A concise view of the academic records that need administrative attention."
      />
      {query.isPending ? (
        <LoadingState />
      ) : query.error ? (
        <ErrorState error={query.error} retry={() => void query.refetch()} />
      ) : (
        <section className={styles.stats}>
          {Object.entries(query.data).map(([label, value]) => (
            <div className={styles.stat} key={label}>
              <span>{label}</span>
              <strong>{value}</strong>
              <small>Current service total</small>
            </div>
          ))}
        </section>
      )}
      <div className={styles.columns}>
        <Panel
          title="Setup sequence"
          description="Complete records in dependency order"
        >
          <ul className={styles.taskList}>
            <Task
              title="Academic catalog"
              detail="Departments, programs, subjects, semesters, and sections"
              to="/admin/academic/departments"
            />
            <Task
              title="Provision people"
              detail="Create the account and academic profile in one workflow"
              to="/admin/people/students"
            />
            <Task
              title="Build enrollments"
              detail="Register students in sections for an active semester"
              to="/admin/enrollments"
            />
          </ul>
        </Panel>
        <Panel title="Account management" description="Automated provisioning">
          <div style={{ padding: 18 }}>
            <p>
              Use the People tab to provision new students and teachers. The system automatically handles their credentials and academic records.
            </p>
            <p className={styles.formNotice}>
              Everything is managed through this portal to ensure records stay synchronized.
            </p>
          </div>
        </Panel>
      </div>
    </>
  );
}
function Task({
  title,
  detail,
  to,
}: {
  title: string;
  detail: string;
  to: string;
}) {
  return (
    <li>
      <div>
        <strong>{title}</strong>
        <span>{detail}</span>
      </div>
      <Link className={styles.link} to={to}>
        Open <ArrowRight size={15} />
      </Link>
    </li>
  );
}
