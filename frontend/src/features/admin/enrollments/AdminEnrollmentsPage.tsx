import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Plus } from "lucide-react";
import { Link } from "react-router-dom";
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
  subjects,
} from "../../../test/fixtures";
export function AdminEnrollmentsPage() {
  const [open, setOpen] = useState(false);
  const [statusFilter, setStatusFilter] = useState<string>("");
  const api = useServiceApi();
  const { session } = useAuth();
  const qc = useQueryClient();
  const query = useQuery({
    queryKey: ["admin", "enrollments", statusFilter],
    queryFn: () =>
      session?.demo ? page(enrollments.filter(e => !statusFilter || e.status === statusFilter)) : api.enrollments(0, 100, { status: statusFilter || undefined }),
  });
  const references = useQuery({
    queryKey: ["admin", "enrollment-references"],
    queryFn: async () => session?.demo ? { students, semesters } : {
      students: (await api.students(0, 100)).content,
      semesters: (await api.semesters(0, 100)).content,
    },
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
          action={
            <select
              className={uiStyles.select}
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="">All statuses</option>
              <option value="ACTIVE">Active</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
          }
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
                      title={studentName(item.studentId, references.data?.students)}
                      detail={references.data?.students.find((student) => student.id === item.studentId)?.studentCode ?? "Student record"}
                    />
                  </td>
                  <td>{semesterName(item.semesterId, references.data?.semesters)}</td>
                  <td>{item.details.length}</td>
                  <td>{item.totalCredits}</td>
                  <td>
                    <StatusBadge value={item.status} />
                  </td>
                  <td>
                    <div style={{ display: "flex", gap: "0.5rem", alignItems: "center" }}>
                      <Link to={`/admin/enrollments/${item.id}`} className={`${uiStyles.button} ${uiStyles.secondary}`} style={{ textDecoration: 'none' }}>
                        Manage
                      </Link>
                      {item.status === "ACTIVE" && (
                        <Button
                          variant="danger"
                          onClick={() => cancel.mutate(item.id)}
                        >
                          Cancel
                        </Button>
                      )}
                    </div>
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
function studentName(id: string, values = students) {
  const value = values.find((item) => item.id === id);
  return value ? `${value.firstName} ${value.lastName}` : id;
}
function semesterName(id: string, values = semesters) {
  return values.find((item) => item.id === id)?.name ?? "Academic term";
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
      if (session?.demo) return { students, semesters, sections, subjects };
      const [s, sem, sec, sub] = await Promise.all([
        api.students(0, 100, undefined, "ACTIVE"),
        api.semesters(0, 100, "ACTIVE"),
        api.sections(0, 100, { status: "ACTIVE" }),
        api.subjects(0, 100, undefined, "ACTIVE"),
      ]);
      return {
        students: s.content,
        semesters: sem.content,
        sections: sec.content,
        subjects: sub.content,
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

  const selectedSections = refs.data?.sections.filter(sec => sectionIds.includes(sec.id)) || [];
  const selectedSubjects = selectedSections.map(sec => sec.subjectId);
  const totalCredits = selectedSections.reduce((acc, sec) => {
    const subject = refs.data?.subjects.find(sub => sub.id === sec.subjectId);
    return acc + (subject?.credits || 0);
  }, 0);

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
            {refs.data?.sections && (
              <div className={uiStyles.sectionGrid}>
                {refs.data.sections
                  .filter((item) => !semesterId || item.semesterId === semesterId)
                  .filter((item) => {
                    const selected = refs.data?.students.find((student) => student.id === studentId);
                    const subject = refs.data?.subjects.find((value) => value.id === item.subjectId);
                    return !selected || !subject || subject.programId === selected.programId;
                  })
                  .map((item) => {
                    const subject = refs.data?.subjects.find((subject) => subject.id === item.subjectId);
                    const isSelected = sectionIds.includes(item.id);
                    const isDuplicateSubject = !isSelected && selectedSubjects.includes(item.subjectId);
                    const isCreditsExceeded = !isSelected && totalCredits + (subject?.credits || 0) > 22;
                    const isDisabled = isDuplicateSubject || isCreditsExceeded;

                    return (
                      <label
                        key={item.id}
                        className={`${uiStyles.sectionCard} ${isSelected ? uiStyles.sectionCardSelected : ""} ${isDisabled ? uiStyles.sectionCardDisabled : ""}`}
                      >
                        <input
                          type="checkbox"
                          checked={isSelected}
                          disabled={isDisabled}
                          onChange={(event) =>
                            setSectionIds((current) =>
                              event.target.checked
                                ? [...current, item.id]
                                : current.filter((id) => id !== item.id),
                            )
                          }
                        />
                        <div className={uiStyles.sectionCardHeader}>
                          <strong>{subject?.name ?? "Subject"} · {item.sectionCode}</strong>
                          <span className={`${uiStyles.sectionBadge} ${uiStyles.badgeSubject}`}>{subject?.credits || 0} CR</span>
                        </div>
                        <div className={uiStyles.sectionCardDetails}>
                          <span>🗓️ {item.schedules[0]?.dayOfWeek} {item.schedules[0]?.startTime}</span>
                          <span>🪑 {item.capacity} seats limit</span>
                          {isDuplicateSubject && <span style={{color: 'var(--red-650)'}}>Subject already selected</span>}
                        </div>
                      </label>
                    );
                  })}
              </div>
            )}
            
            <div className={`${uiStyles.creditsCounter} ${totalCredits > 22 ? uiStyles.creditsExceeded : ""}`}>
              <span>Selected Credits: <strong>{totalCredits}</strong> / 22 max</span>
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
