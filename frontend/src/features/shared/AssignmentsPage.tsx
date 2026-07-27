import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Plus } from "lucide-react";
import { useSearchParams } from "react-router-dom";
import type { Assignment } from "../../api/generated/contracts";
import { useServiceApi } from "../../api/use-service-api";
import { useAuth } from "../../auth/AuthProvider";
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
} from "../../components/ui";
import {
  assignments,
  page,
  sections,
  students,
  subjects,
  submissions,
  teacherSections,
} from "../../test/fixtures";
import styles from "../feature.module.css";
import { teacherSectionLabel } from "../teacher/teacher-section";
type AssignmentSection = {
  id: string;
  label: string;
  sectionCode: string;
};
export function AssignmentsPage() {
  const api = useServiceApi();
  const { session } = useAuth();
  const qc = useQueryClient();
  const teacher = session?.role === "TEACHER";
  const [searchParams, setSearchParams] = useSearchParams();
  const [sectionId, setSectionId] = useState("");
  const [creating, setCreating] = useState(false);
  const [submitting, setSubmitting] = useState<string>();
  const [reviewing, setReviewing] = useState<string>();
  const sectionQuery = useQuery<AssignmentSection[]>({
    queryKey: [session?.role, "assignment-sections"],
    queryFn: async () => {
      if (session?.demo) {
        return teacher
          ? teacherSections.map((section) => ({
              id: section.id,
              sectionCode: section.sectionCode,
              label: teacherSectionLabel(section),
            }))
          : sections.map((section) => {
              const subject = subjects.find((item) => item.id === section.subjectId);
              return {
                id: section.id,
                sectionCode: section.sectionCode,
                label: subject
                  ? `${subject.code} — ${subject.name} · ${section.sectionCode}`
                  : section.sectionCode,
              };
            });
      }
      if (teacher) {
        return (await api.teacherSections()).map((section) => ({
          id: section.id,
          sectionCode: section.sectionCode,
          label: teacherSectionLabel(section),
        }));
      }
      const records = await api.myEnrollments(0, 100, undefined, "ACTIVE");
      return records.content.flatMap((item) =>
        item.details.map((detail) => ({
          id: detail.sectionId,
          sectionCode: detail.section.sectionCode,
          label: `${detail.subject.code} — ${detail.subject.name} · ${detail.section.sectionCode}`,
        })),
      );
    },
  });
  const requestedSectionId = searchParams.get("sectionId") ?? "";
  const availableSections = sectionQuery.data ?? [];
  const selectedSection = availableSections.some((section) => section.id === sectionId)
    ? sectionId
    : availableSections.some((section) => section.id === requestedSectionId)
      ? requestedSectionId
      : availableSections[0]?.id ?? "";
  const activeSection = selectedSection;
  const query = useQuery({
    queryKey: [session?.role, "assignments", activeSection],
    enabled: Boolean(activeSection),
    queryFn: () =>
      session?.demo
        ? page(assignments)
        : api.assignments(activeSection, 0, 100, teacher ? undefined : "PUBLISHED", !teacher),
  });
  const action = useMutation({
    mutationFn: ({ id, type }: { id: string; type: "publish" | "close" }) =>
      type === "publish" ? api.publishAssignment(id) : api.closeAssignment(id),
    onSuccess: () =>
      void qc.invalidateQueries({
        queryKey: [session?.role, "assignments", activeSection],
      }),
  });
  return (
    <>
      <PageHeader
        eyebrow={teacher ? "Coursework and grading" : "Coursework"}
        title="Assignments"
        description={
          teacher
            ? "Draft, publish, and close work within an assigned section."
            : "Review published work, submit a response, and see released grades."
        }
        action={
          teacher ? (
            <Button onClick={() => setCreating(true)}>
              <Plus size={17} />
              New assignment
            </Button>
          ) : undefined
        }
      />
      <div className={styles.catalogNav}>
        {sectionQuery.data?.map((section) => (
          <button
            key={section.id}
            className={activeSection === section.id ? styles.active : ""}
            onClick={() => {
              setSectionId(section.id);
              setSearchParams({ sectionId: section.id });
            }}
          >
            {section.label}
          </button>
        ))}
      </div>
      {query.isPending ? (
        <LoadingState />
      ) : query.error ? (
        <ErrorState error={query.error} retry={() => void query.refetch()} />
      ) : query.data.content.length === 0 ? (
        <EmptyState
          title="No assignments in this section"
          description={
            teacher
              ? "Create a draft when the next piece of coursework is ready."
              : "Published coursework will appear here."
          }
        />
      ) : (
        <Panel
          title="Section assignments"
          description={`${query.data.totalElements} records`}
        >
          <DataTable>
            <thead>
              <tr>
                <th>Assignment</th>
                <th>Due</th>
                <th>Points</th>
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
                    <PrimaryCell title={item.title} detail={item.description} />
                  </td>
                  <td>{new Date(item.dueAt).toLocaleString()}</td>
                  <td>{item.maxPoints}</td>
                  <td>
                    <StatusBadge value={item.status} />
                  </td>
                  <td>
                    {teacher ? (
                      <>
                        <Button
                          variant="secondary"
                          onClick={() => setReviewing(item.id)}
                        >
                          Review submissions
                        </Button>{" "}
                        {item.status === "DRAFT" && (
                          <Button
                            variant="secondary"
                            onClick={() =>
                              action.mutate({ id: item.id, type: "publish" })
                            }
                          >
                            Publish
                          </Button>
                        )}
                        {item.status === "PUBLISHED" && (
                          <Button
                            variant="danger"
                            onClick={() =>
                              action.mutate({ id: item.id, type: "close" })
                            }
                          >
                            Close
                          </Button>
                        )}
                      </>
                    ) : (
                      <StudentAssignmentAction
                        assignment={item}
                        onSubmit={() => setSubmitting(item.id)}
                      />
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </DataTable>
        </Panel>
      )}
      {creating && (
        <AssignmentDialog
          sectionId={activeSection}
          onClose={() => setCreating(false)}
        />
      )}
      {submitting && (
        <SubmissionDialog
          assignmentId={submitting}
          onClose={() => setSubmitting(undefined)}
        />
      )}
      {reviewing && (
        <SubmissionReviewDialog
          assignmentId={reviewing}
          onClose={() => setReviewing(undefined)}
        />
      )}
    </>
  );
}

function StudentAssignmentAction({
  assignment,
  onSubmit,
}: {
  assignment: Assignment;
  onSubmit: () => void;
}) {
  const api = useServiceApi();
  const { session } = useAuth();
  const query = useQuery({
    queryKey: ["student", "submission", assignment.id],
    queryFn: () =>
      session?.demo
        ? page(
            submissions.filter((item) => item.assignmentId === assignment.id),
          )
        : api.submissions(assignment.id, 0, 10, true),
  });
  const submission = query.data?.content[0];
  if (query.isPending) return <span className={uiStyles.muted}>Checking…</span>;
  if (submission?.gradeReleased)
    return (
      <PrimaryCell
        title={`${submission.score ?? "—"} / ${assignment.maxPoints}`}
        detail="Grade released"
      />
    );
  return (
    <div>
      {submission && <StatusBadge value="SUBMITTED" />}{" "}
      <Button variant="secondary" onClick={onSubmit}>
        {submission ? "Update submission" : "Submit work"}
      </Button>
    </div>
  );
}

function SubmissionReviewDialog({
  assignmentId,
  onClose,
}: {
  assignmentId: string;
  onClose: () => void;
}) {
  const api = useServiceApi();
  const { session } = useAuth();
  const qc = useQueryClient();
  const [scores, setScores] = useState<Record<string, string>>({});
  const [feedback, setFeedback] = useState<Record<string, string>>({});
  const query = useQuery({
    queryKey: ["teacher", "submissions", assignmentId],
    queryFn: () =>
      session?.demo
        ? page(submissions.filter((item) => item.assignmentId === assignmentId))
        : api.submissions(assignmentId, 0, 100),
  });
  const grade = useMutation({
    mutationFn: (id: string) =>
      session?.demo
        ? Promise.resolve({})
        : api.gradeSubmission(
            id,
            Number(scores[id]),
            feedback[id] || undefined,
          ),
    onSuccess: () =>
      void qc.invalidateQueries({
        queryKey: ["teacher", "submissions", assignmentId],
      }),
  });
  const release = useMutation({
    mutationFn: (id: string) =>
      session?.demo ? Promise.resolve({}) : api.releaseGrade(id),
    onSuccess: () =>
      void qc.invalidateQueries({
        queryKey: ["teacher", "submissions", assignmentId],
      }),
  });
  return (
    <Dialog
      title="Review submissions"
      description="Grade submitted work, then release the result when it is ready for the student."
      onClose={onClose}
    >
      {query.isPending ? (
        <LoadingState label="Loading submissions…" />
      ) : query.error ? (
        <ErrorState error={query.error} />
      ) : query.data.content.length === 0 ? (
        <EmptyState
          title="No submissions received"
          description="Student work will appear here after it is submitted."
        />
      ) : (
        <DataTable>
          <thead>
            <tr>
              <th>Student and response</th>
              <th>Score</th>
              <th>Feedback</th>
              <th>Grade</th>
            </tr>
          </thead>
          <tbody>
            {query.data.content.map((item) => {
              const student = students.find(
                (value) => value.id === item.studentId,
              );
              return (
                <tr key={item.id}>
                  <td>
                    <PrimaryCell
                      title={
                        student
                          ? `${student.firstName} ${student.lastName}`
                          : "Enrolled student"
                      }
                      detail={item.content}
                    />
                  </td>
                  <td>
                    <input
                      className={uiStyles.select}
                      aria-label="Score"
                      type="number"
                      min="0"
                      value={scores[item.id] ?? item.score ?? ""}
                      onChange={(event) =>
                        setScores((current) => ({
                          ...current,
                          [item.id]: event.target.value,
                        }))
                      }
                    />
                  </td>
                  <td>
                    <input
                      className={uiStyles.select}
                      aria-label="Feedback"
                      value={feedback[item.id] ?? item.feedback ?? ""}
                      onChange={(event) =>
                        setFeedback((current) => ({
                          ...current,
                          [item.id]: event.target.value,
                        }))
                      }
                    />
                  </td>
                  <td>
                    {item.gradeReleased ? (
                      <StatusBadge value="RELEASED" />
                    ) : item.score !== undefined ? (
                      <Button
                        variant="secondary"
                        onClick={() => release.mutate(item.id)}
                      >
                        Release
                      </Button>
                    ) : (
                      <Button
                        variant="secondary"
                        disabled={!scores[item.id]}
                        onClick={() => grade.mutate(item.id)}
                      >
                        Save grade
                      </Button>
                    )}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </DataTable>
      )}
      <div className={uiStyles.actions}>
        <Button variant="secondary" onClick={onClose}>
          Close
        </Button>
      </div>
    </Dialog>
  );
}
function AssignmentDialog({
  sectionId,
  onClose,
}: {
  sectionId: string;
  onClose: () => void;
}) {
  const api = useServiceApi();
  const { session } = useAuth();
  const qc = useQueryClient();
  const [title, setTitle] = useState("");
  const [description, setDescription] = useState("");
  const [dueAt, setDueAt] = useState("");
  const [points, setPoints] = useState("100");
  const mutation = useMutation({
    mutationFn: () =>
      session?.demo
        ? Promise.resolve({})
        : api.createAssignment({
            sectionId,
            title,
            description,
            dueAt: new Date(dueAt).toISOString(),
            maxPoints: Number(points),
          }),
    onSuccess: () => {
      void qc.invalidateQueries({
        queryKey: ["TEACHER", "assignments", sectionId],
      });
      onClose();
    },
  });
  return (
    <Dialog
      title="New assignment"
      description="The assignment starts as a draft and remains invisible to students until published."
      onClose={onClose}
    >
      <form
        onSubmit={(event) => {
          event.preventDefault();
          mutation.mutate();
        }}
      >
        <div className={uiStyles.formGrid}>
          <Field label="Title" className={uiStyles.span2}>
            <input
              required
              value={title}
              onChange={(event) => setTitle(event.target.value)}
            />
          </Field>
          <Field label="Due date and time">
            <input
              required
              type="datetime-local"
              value={dueAt}
              onChange={(event) => setDueAt(event.target.value)}
            />
          </Field>
          <Field label="Maximum points">
            <input
              required
              type="number"
              min="1"
              value={points}
              onChange={(event) => setPoints(event.target.value)}
            />
          </Field>
          <Field label="Instructions" className={uiStyles.span2}>
            <textarea
              value={description}
              onChange={(event) => setDescription(event.target.value)}
            />
          </Field>
        </div>
        {mutation.error && (
          <p className={uiStyles.errorText}>{mutation.error.message}</p>
        )}
        <div className={uiStyles.actions}>
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button disabled={mutation.isPending}>Save draft</Button>
        </div>
      </form>
    </Dialog>
  );
}
function SubmissionDialog({
  assignmentId,
  onClose,
}: {
  assignmentId: string;
  onClose: () => void;
}) {
  const api = useServiceApi();
  const { session } = useAuth();
  const [content, setContent] = useState("");
  const mutation = useMutation({
    mutationFn: () =>
      session?.demo
        ? Promise.resolve({})
        : api.submitAssignment(assignmentId, content),
  });
  return (
    <Dialog
      title="Submit assignment"
      description="Review your response before submitting. A later submission may be marked late."
      onClose={onClose}
    >
      {mutation.data ? (
        <EmptyState
          title="Submission received"
          description="Your work has been recorded for this assignment."
          action={<Button onClick={onClose}>Return to assignments</Button>}
        />
      ) : (
        <form
          onSubmit={(event) => {
            event.preventDefault();
            mutation.mutate();
          }}
        >
          <Field label="Response">
            <textarea
              required
              value={content}
              onChange={(event) => setContent(event.target.value)}
            />
          </Field>
          {mutation.error && (
            <p className={uiStyles.errorText}>{mutation.error.message}</p>
          )}
          <div className={uiStyles.actions}>
            <Button type="button" variant="secondary" onClick={onClose}>
              Cancel
            </Button>
            <Button disabled={mutation.isPending}>
              {mutation.isPending ? "Submitting…" : "Submit work"}
            </Button>
          </div>
        </form>
      )}
    </Dialog>
  );
}
