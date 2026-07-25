import { useMemo, useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { Plus, Save } from "lucide-react";
import { useParams } from "react-router-dom";
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
import {
  attendanceSessions,
  page,
  sections,
  students,
} from "../../../test/fixtures";
import styles from "../../feature.module.css";
const attendanceValues = ["PRESENT", "ABSENT", "LATE", "EXCUSED"] as const;
export function AttendancePage() {
  const { sectionId = "" } = useParams();
  const api = useServiceApi();
  const { session } = useAuth();
  const qc = useQueryClient();
  const [selected, setSelected] = useState<string>();
  const [creating, setCreating] = useState(false);
  const [marks, setMarks] = useState<Record<string, string>>({});
  const sessionQuery = useQuery({
    queryKey: ["attendance", sectionId, "sessions"],
    enabled: Boolean(sectionId),
    queryFn: () =>
      session?.demo
        ? page(attendanceSessions)
        : api.attendanceSessions(sectionId, 0, 100),
  });
  const rosterQuery = useQuery({
    queryKey: ["attendance", sectionId, "roster"],
    enabled: Boolean(sectionId),
    queryFn: () =>
      session?.demo
        ? { sectionId, studentIds: students.map((item) => item.id) }
        : api.sectionStudents(sectionId),
  });
  const activeSession = selected ?? sessionQuery.data?.content[0]?.id;
  const roster = useMemo(
    () => rosterQuery.data?.studentIds ?? [],
    [rosterQuery.data],
  );
  const save = useMutation({
    mutationFn: () =>
      session?.demo
        ? Promise.resolve([])
        : api.recordAttendance(
            activeSession!,
            roster.map((studentId) => ({
              studentId,
              status: marks[studentId] ?? "PRESENT",
            })),
          ),
    onSuccess: () =>
      void qc.invalidateQueries({
        queryKey: ["attendance", activeSession, "records"],
      }),
  });
  if (!sectionId)
    return (
      <EmptyState
        title="Choose a section"
        description="Open attendance from one of your assigned sections."
      />
    );
  if (sessionQuery.isPending || rosterQuery.isPending)
    return <LoadingState label="Preparing the attendance register…" />;
  if (sessionQuery.error || rosterQuery.error)
    return (
      <ErrorState
        error={sessionQuery.error ?? rosterQuery.error}
        retry={() => {
          void sessionQuery.refetch();
          void rosterQuery.refetch();
        }}
      />
    );
  const section = sections.find((item) => item.id === sectionId);
  return (
    <>
      <PageHeader
        eyebrow="Attendance register"
        title={section?.sectionCode ?? "Section attendance"}
        description="Create a class meeting, then mark each enrolled student before saving the register."
        action={
          <Button onClick={() => setCreating(true)}>
            <Plus size={17} />
            New session
          </Button>
        }
      />
      <div className={styles.attendanceGrid}>
        <Panel
          title="Class meetings"
          description={`${sessionQuery.data.content.length} sessions`}
        >
          <ul className={styles.sessionList}>
            {sessionQuery.data.content.map((item) => (
              <li key={item.id}>
                <button
                  className={`${styles.sessionButton} ${activeSession === item.id ? styles.selected : ""}`}
                  onClick={() => setSelected(item.id)}
                >
                  <strong>
                    Session {item.sessionNumber} · {item.date}
                  </strong>
                  <span>{item.topic || "No topic recorded"}</span>
                </button>
              </li>
            ))}
          </ul>
        </Panel>
        {!activeSession ? (
          <EmptyState
            title="Create the first class meeting"
            description="Attendance marks belong to a dated session."
          />
        ) : (
          <Panel
            title="Student register"
            description={`${roster.length} enrolled students`}
            action={
              <Button onClick={() => save.mutate()} disabled={save.isPending}>
                <Save size={16} />
                {save.isPending ? "Saving…" : "Save register"}
              </Button>
            }
          >
            <DataTable>
              <thead>
                <tr>
                  <th>Student</th>
                  <th>Attendance mark</th>
                </tr>
              </thead>
              <tbody>
                {roster.map((id, index) => {
                  const person = students.find((item) => item.id === id);
                  return (
                    <tr key={id}>
                      <td>
                        <PrimaryCell
                          title={
                            person
                              ? `${person.firstName} ${person.lastName}`
                              : `Enrolled student ${index + 1}`
                          }
                          detail={person?.studentCode ?? "Roster member"}
                        />
                      </td>
                      <td>
                        <div className={styles.attendanceOptions}>
                          {attendanceValues.map((value) => (
                            <label key={value}>
                              <input
                                type="radio"
                                name={id}
                                value={value}
                                checked={(marks[id] ?? "PRESENT") === value}
                                onChange={() =>
                                  setMarks((current) => ({
                                    ...current,
                                    [id]: value,
                                  }))
                                }
                              />
                              <span>{value}</span>
                            </label>
                          ))}
                        </div>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </DataTable>
            {save.error && (
              <p className={uiStyles.errorText}>{save.error.message}</p>
            )}
          </Panel>
        )}
      </div>
      {creating && (
        <SessionDialog
          sectionId={sectionId}
          onClose={() => setCreating(false)}
        />
      )}
    </>
  );
}
function SessionDialog({
  sectionId,
  onClose,
}: {
  sectionId: string;
  onClose: () => void;
}) {
  const api = useServiceApi();
  const { session } = useAuth();
  const qc = useQueryClient();
  const [number, setNumber] = useState("");
  const [date, setDate] = useState(new Date().toISOString().slice(0, 10));
  const [topic, setTopic] = useState("");
  const mutation = useMutation({
    mutationFn: () =>
      session?.demo
        ? Promise.resolve({})
        : api.createAttendanceSession({
            sectionId,
            sessionNumber: Number(number),
            date,
            topic,
          }),
    onSuccess: () => {
      void qc.invalidateQueries({
        queryKey: ["attendance", sectionId, "sessions"],
      });
      onClose();
    },
  });
  return (
    <Dialog
      title="New class meeting"
      description="Attendance is recorded against this dated session."
      onClose={onClose}
    >
      <form
        onSubmit={(event) => {
          event.preventDefault();
          mutation.mutate();
        }}
      >
        <div className={uiStyles.formGrid}>
          <Field label="Session number">
            <input
              required
              type="number"
              min="1"
              value={number}
              onChange={(event) => setNumber(event.target.value)}
            />
          </Field>
          <Field label="Date">
            <input
              required
              type="date"
              value={date}
              onChange={(event) => setDate(event.target.value)}
            />
          </Field>
          <Field label="Topic" className={uiStyles.span2}>
            <input
              value={topic}
              onChange={(event) => setTopic(event.target.value)}
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
          <Button disabled={mutation.isPending}>
            {mutation.isPending ? "Creating…" : "Create session"}
          </Button>
        </div>
      </form>
    </Dialog>
  );
}
