import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link } from "react-router-dom";
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
import styles from "../../feature.module.css";

export function StudentEnrollmentsPage() {
  const [open, setOpen] = useState(false);
  const [hideOld, setHideOld] = useState(true);
  const api = useServiceApi();
  const { session } = useAuth();
  const query = useQuery({
    queryKey: ["student", "enrollments"],
    queryFn: () =>
      api.myEnrollments(0, 100),
  });

  return (
    <>
      <PageHeader
        eyebrow="Academic registration"
        title="My enrollments"
        description="Your current and previous semester registrations, scoped to your signed-in account."
        action={
          <Button onClick={() => setOpen(true)}>
            <Plus size={17} />
            Register for Semester
          </Button>
        }
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
          action={
            <label style={{ display: "flex", alignItems: "center", gap: "8px", fontSize: "0.9rem", cursor: "pointer" }}>
              <input 
                type="checkbox" 
                checked={hideOld} 
                onChange={(e) => setHideOld(e.target.checked)} 
              />
              Hide cancelled/old
            </label>
          }
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
              {query.data.content
                .filter((item) => !hideOld || item.status === "ACTIVE")
                .map((item) => (
                <tr key={item.id}>
                  <td>
                    <Link to={`/student/enrollments/${item.id}`} style={{ textDecoration: 'none', color: 'inherit' }}>
                      <PrimaryCell
                        title={item.semester.name}
                        detail={`Registered ${new Date(item.createdAt).toLocaleDateString()}`}
                      />
                    </Link>
                  </td>
                  <td>
                    {item.details.map((detail) => (
                      <Link
                        className={styles.link}
                        key={detail.id}
                        to={`/student/classes/${detail.sectionId}`}
                        style={{ display: "block", paddingBottom: "4px" }}
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
      {open && <StudentEnrollmentDialog onClose={() => setOpen(false)} />}
    </>
  );
}

function StudentEnrollmentDialog({ onClose }: { onClose: () => void }) {
  const api = useServiceApi();
  const { session, profileId } = useAuth();
  const qc = useQueryClient();
  const [semesterId, setSemesterId] = useState("");
  const [sectionIds, setSectionIds] = useState<string[]>([]);
  const refs = useQuery({
    queryKey: ["student-enrollment", "references"],
    queryFn: async () => {
      const [sem, sec, student] = await Promise.all([
        api.semesters(0, 100, "ACTIVE"),
        api.sections(0, 100, { status: "ACTIVE" }),
        api.studentMe(),
      ]);
      const sub = await api.subjects(0, 100, student.programId, "ACTIVE");
      return {
        semesters: sem.content.filter(s => s.isRegistrationOpen),
        sections: sec.content,
        subjects: sub.content,
        studentId: student.id,
      };
    },
  });
  const mutation = useMutation({
    mutationFn: () =>
      api.createEnrollment({ studentId: refs.data!.studentId, semesterId, sectionIds }),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ["student", "enrollments"] });
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
      title="Register for Semester"
      description="Select an open semester and your classes."
      onClose={onClose}
    >
      <form
        onSubmit={(event) => {
          event.preventDefault();
          mutation.mutate();
        }}
      >
        <div className={uiStyles.formGrid}>
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
          {refs.data?.semesters.length === 0 && (
            <p className={uiStyles.errorText}>No semesters are currently open for registration.</p>
          )}
          <Field label="Sections" className={uiStyles.span2}>
            {refs.data?.sections && semesterId && (
              <div className={uiStyles.sectionGrid}>
                {refs.data.sections
                  .filter((item) => item.semesterId === semesterId)
                  .filter((item) => refs.data?.subjects.some(sub => sub.id === item.subjectId))
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
          <Button disabled={mutation.isPending || sectionIds.length === 0 || !semesterId}>
            {mutation.isPending ? "Registering…" : "Register"}
          </Button>
        </div>
      </form>
    </Dialog>
  );
}
