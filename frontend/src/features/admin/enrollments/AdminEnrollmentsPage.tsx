import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Plus } from "lucide-react";
import { useServiceApi } from "../../../api/use-service-api";
import { useAuth } from "../../../auth/AuthProvider";
import {
  Button,
  DataTable,
  Dialog,
  EmptyState,
  ErrorState,
  Field,
  LoadingState,
  PageHeader,
  Panel,
  PrimaryCell,
  StatusBadge,
  uiStyles,
} from "../../../components/ui";
import {
  enrollments,
  page,
  sections,
  semesters,
  students,
} from "../../../test/fixtures";
export function AdminEnrollmentsPage() {
  const [open, setOpen] = useState(false);
  const api = useServiceApi();
  const { session } = useAuth();
  const qc = useQueryClient();
  const query = useQuery({
    queryKey: ["admin", "enrollments"],
    queryFn: () =>
      session?.demo ? page(enrollments) : api.enrollments(0, 100),
  });
  const cancel = useMutation({
    mutationFn: (id: string) =>
      session?.demo ? Promise.resolve({}) : api.cancelEnrollment(id),
    onSuccess: () =>
      void qc.invalidateQueries({ queryKey: ["admin", "enrollments"] }),
  });
  return (
    <>
      <PageHeader
        eyebrow="Registration"
        title="Enrollments"
        description="Register a student in one or more sections for an academic semester."
        action={
          <Button onClick={() => setOpen(true)}>
            <Plus size={17} />
            Create enrollment
          </Button>
        }
      />
      {query.isPending ? (
        <LoadingState />
      ) : query.error ? (
        <ErrorState error={query.error} retry={() => void query.refetch()} />
      ) : query.data.content.length === 0 ? (
        <EmptyState
          title="No enrollments yet"
          description="Create an enrollment after students, semesters, and sections are active."
        />
      ) : (
        <Panel
          title="Enrollment records"
          description={`${query.data.totalElements} records`}
        >
          <DataTable>
            <thead>
              <tr>
                <th>Student</th>
                <th>Semester</th>
                <th>Sections</th>
                <th>Credits</th>
                <th>Status</th>
                <th>
                  <span className="sr-only">Actions</span>
                </th>
              </tr>
            </thead>
            <tbody>
              {query.data.content.map((item) => (
                <tr key={item.id}>
                  <td>
                    <PrimaryCell
                      title={studentName(item.studentId)}
                      detail={item.studentId}
                    />
                  </td>
                  <td>{semesterName(item.semesterId)}</td>
                  <td>{item.details.length}</td>
                  <td>{item.totalCredits}</td>
                  <td>
                    <StatusBadge value={item.status} />
                  </td>
                  <td>
                    {item.status === "ACTIVE" && (
                      <Button
                        variant="danger"
                        onClick={() => cancel.mutate(item.id)}
                      >
                        Cancel
                      </Button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </DataTable>
        </Panel>
      )}
      {open && <EnrollmentDialog onClose={() => setOpen(false)} />}
    </>
  );
}
function studentName(id: string) {
  const value = students.find((item) => item.id === id);
  return value ? `${value.firstName} ${value.lastName}` : id;
}
function semesterName(id: string) {
  return semesters.find((item) => item.id === id)?.name ?? id;
}
function EnrollmentDialog({ onClose }: { onClose: () => void }) {
  const api = useServiceApi();
  const { session } = useAuth();
  const qc = useQueryClient();
  const [studentId, setStudentId] = useState("");
  const [semesterId, setSemesterId] = useState("");
  const [sectionIds, setSectionIds] = useState<string[]>([]);
  const refs = useQuery({
    queryKey: ["enrollment", "references"],
    queryFn: async () => {
      if (session?.demo) return { students, semesters, sections };
      const [s, sem, sec] = await Promise.all([
        api.students(0, 100, undefined, "ACTIVE"),
        api.semesters(0, 100, "ACTIVE"),
        api.sections(0, 100, { status: "ACTIVE" }),
      ]);
      return {
        students: s.content,
        semesters: sem.content,
        sections: sec.content,
      };
    },
  });
  const mutation = useMutation({
    mutationFn: () =>
      session?.demo
        ? Promise.resolve({})
        : api.createEnrollment({ studentId, semesterId, sectionIds }),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ["admin", "enrollments"] });
      onClose();
    },
  });
  return (
    <Dialog
      title="Create enrollment"
      description="Choose records by name; internal profile identifiers stay out of view."
      onClose={onClose}
    >
      <form
        onSubmit={(event) => {
          event.preventDefault();
          mutation.mutate();
        }}
      >
        <div className={uiStyles.formGrid}>
          <Field label="Student">
            <select
              required
              value={studentId}
              onChange={(event) => setStudentId(event.target.value)}
            >
              <option value="">Select a student</option>
              {refs.data?.students.map((item) => (
                <option key={item.id} value={item.id}>
                  {item.studentCode} — {item.firstName} {item.lastName}
                </option>
              ))}
            </select>
          </Field>
          <Field label="Semester">
            <select
              required
              value={semesterId}
              onChange={(event) => setSemesterId(event.target.value)}
            >
              <option value="">Select a semester</option>
              {refs.data?.semesters.map((item) => (
                <option key={item.id} value={item.id}>
                  {item.name}
                </option>
              ))}
            </select>
          </Field>
          <Field label="Sections" className={uiStyles.span2}>
            <div>
              {refs.data?.sections
                .filter((item) => !semesterId || item.semesterId === semesterId)
                .map((item) => (
                  <label
                    key={item.id}
                    style={{ display: "flex", gap: 8, padding: "7px 0" }}
                  >
                    <input
                      type="checkbox"
                      checked={sectionIds.includes(item.id)}
                      onChange={(event) =>
                        setSectionIds((current) =>
                          event.target.checked
                            ? [...current, item.id]
                            : current.filter((id) => id !== item.id),
                        )
                      }
                    />
                    {item.sectionCode} · {item.schedules[0]?.dayOfWeek}{" "}
                    {item.schedules[0]?.startTime}
                  </label>
                ))}
            </div>
          </Field>
        </div>
        {mutation.error && (
          <p className={uiStyles.errorText}>{mutation.error.message}</p>
        )}
        <div className={uiStyles.actions}>
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button disabled={mutation.isPending || sectionIds.length === 0}>
            {mutation.isPending ? "Registering…" : "Create enrollment"}
          </Button>
        </div>
      </form>
    </Dialog>
  );
}
