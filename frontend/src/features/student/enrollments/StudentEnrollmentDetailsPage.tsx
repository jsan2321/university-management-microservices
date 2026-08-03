import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Link, useParams } from "react-router-dom";
import { ChevronLeft, Plus } from "lucide-react";
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
  uiStyles,
} from "../../../components/ui";
import styles from "../../feature.module.css";

export function StudentEnrollmentDetailsPage() {
  const { id } = useParams<{ id: string }>();
  const [open, setOpen] = useState(false);
  const api = useServiceApi();
  const { session } = useAuth();
  const qc = useQueryClient();

  const query = useQuery({
    queryKey: ["student", "enrollments", id],
    queryFn: () => {
      if (session?.demo) return Promise.reject(new Error("Demo mode not fully supported here"));
      return api.enrollment(id!);
    },
    enabled: !!id,
  });

  const drop = useMutation({
    mutationFn: (sectionId: string) => api.dropEnrollmentSection(id!, sectionId),
    onSuccess: () => void qc.invalidateQueries({ queryKey: ["student", "enrollments", id] }),
  });

  if (query.isPending) return <LoadingState />;
  if (query.error) return <ErrorState error={query.error} retry={() => void query.refetch()} />;
  
  const enrollment = query.data;

  return (
    <>
      <PageHeader
        eyebrow={
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Link to="/student/enrollments" style={{ display: 'inline-flex', alignItems: 'center', textDecoration: 'none', color: 'inherit' }}>
              <ChevronLeft size={16} /> Back
            </Link>
            <span>/</span>
            <span>Registration Details</span>
          </div>
        }
        title={`Enrollment: ${enrollment.semester.name}`}
        description={`Status: ${enrollment.status} · Total Credits: ${enrollment.totalCredits} ${enrollment.isRegistrationOpen ? "· Registration: OPEN" : ""}`}
        action={
          enrollment.isRegistrationOpen && enrollment.status === "ACTIVE" ? (
            <Button onClick={() => setOpen(true)}>
              <Plus size={17} />
              Add Class
            </Button>
          ) : null
        }
      />
      
      {enrollment.details.length === 0 ? (
        <EmptyState
          title="No classes found"
          description="You are not enrolled in any classes for this semester."
        />
      ) : (
        <Panel
          title="Classes"
          description={`${enrollment.details.length} enrolled sections`}
        >
          <DataTable>
            <thead>
              <tr>
                <th>Subject</th>
                <th>Section</th>
                <th>Credits</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {enrollment.details.map((detail) => (
                <tr key={detail.id}>
                  <td>
                    <PrimaryCell
                      title={detail.subject.name}
                      detail={detail.subject.code}
                    />
                  </td>
                  <td>
                    <Link
                      className={styles.link}
                      to={`/student/sections/${detail.sectionId}`}
                    >
                      {detail.section.sectionCode}
                    </Link>
                  </td>
                  <td>{detail.credits}</td>
                  <td>
                    {enrollment.isRegistrationOpen && enrollment.status === "ACTIVE" && (
                      <Button
                        variant="danger"
                        onClick={() => {
                          if (confirm(`Are you sure you want to drop ${detail.subject.name}?`)) {
                            drop.mutate(detail.sectionId);
                          }
                        }}
                      >
                        Drop
                      </Button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </DataTable>
        </Panel>
      )}

      {open && <AddSectionDialog enrollmentId={id!} semesterId={enrollment.semesterId} onClose={() => setOpen(false)} />}
    </>
  );
}

function AddSectionDialog({ enrollmentId, semesterId, onClose }: { enrollmentId: string; semesterId: string; onClose: () => void }) {
  const api = useServiceApi();
  const qc = useQueryClient();
  const [sectionId, setSectionId] = useState("");

  const refs = useQuery({
    queryKey: ["enrollment", "available-sections", semesterId],
    queryFn: async () => {
      const [sec, student] = await Promise.all([
        api.sections(0, 100, { semesterId, status: "ACTIVE" }),
        api.studentMe(),
      ]);
      const sub = await api.subjects(0, 100, student.programId, "ACTIVE");
      return {
        sections: sec.content,
        subjects: sub.content,
      };
    },
  });

  const mutation = useMutation({
    mutationFn: () => api.addEnrollmentSection(enrollmentId, sectionId),
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ["student", "enrollments", enrollmentId] });
      onClose();
    },
  });

  const selectedSection = refs.data?.sections.find(s => s.id === sectionId);
  const selectedSubject = refs.data?.subjects.find(s => s.id === selectedSection?.subjectId);

  return (
    <Dialog
      title="Add Class"
      description="Select a class to add to your current schedule."
      onClose={onClose}
    >
      <form
        onSubmit={(event) => {
          event.preventDefault();
          mutation.mutate();
        }}
      >
        <div className={uiStyles.formGrid}>
          <Field label="Available Sections" className={uiStyles.span2}>
            {refs.data?.sections && (
              <div className={uiStyles.sectionGrid}>
                {refs.data.sections
                  .filter((item) => refs.data?.subjects.some(sub => sub.id === item.subjectId))
                  .map((item) => {
                  const subject = refs.data?.subjects.find((sub) => sub.id === item.subjectId);
                  const isSelected = sectionId === item.id;

                  return (
                    <label
                      key={item.id}
                      className={`${uiStyles.sectionCard} ${isSelected ? uiStyles.sectionCardSelected : ""}`}
                    >
                      <input
                        type="radio"
                        name="sectionId"
                        value={item.id}
                        checked={isSelected}
                        onChange={(event) => setSectionId(event.target.value)}
                      />
                      <div className={uiStyles.sectionCardHeader}>
                        <strong>{subject?.name ?? "Subject"} · {item.sectionCode}</strong>
                        <span className={`${uiStyles.sectionBadge} ${uiStyles.badgeSubject}`}>{subject?.credits || 0} CR</span>
                      </div>
                      <div className={uiStyles.sectionCardDetails}>
                        <span>🗓️ {item.schedules[0]?.dayOfWeek} {item.schedules[0]?.startTime}</span>
                        <span>🪑 {item.capacity} seats limit</span>
                      </div>
                    </label>
                  );
                })}
              </div>
            )}
          </Field>
        </div>
        {mutation.error && (
          <p className={uiStyles.errorText}>{mutation.error.message}</p>
        )}
        <div className={uiStyles.actions}>
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button disabled={mutation.isPending || !sectionId}>
            {mutation.isPending ? "Adding…" : "Add class"}
          </Button>
        </div>
      </form>
    </Dialog>
  );
}
