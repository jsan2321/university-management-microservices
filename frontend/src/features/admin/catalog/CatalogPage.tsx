import { useState } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { NavLink, useParams } from "react-router-dom";
import { Plus } from "lucide-react";
import type {
  Department,
  Program,
  Section,
  Semester,
  Subject,
} from "../../../api/generated/contracts";
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
  departments,
  page,
  programs,
  sections,
  semesters,
  subjects,
  teachers,
} from "../../../test/fixtures";
import styles from "../../feature.module.css";

type Kind = "departments" | "programs" | "subjects" | "semesters" | "sections";
type Item = Department | Program | Subject | Semester | Section;
const kinds: Kind[] = [
  "departments",
  "programs",
  "subjects",
  "semesters",
  "sections",
];
const singular: Record<Kind, string> = {
  departments: "department",
  programs: "program",
  subjects: "subject",
  semesters: "semester",
  sections: "section",
};
export function CatalogPage() {
  const rawKind = useParams().kind;
  const kind: Kind = kinds.includes(rawKind as Kind)
    ? (rawKind as Kind)
    : "departments";
  const [creating, setCreating] = useState(false);
  const api = useServiceApi();
  const { session } = useAuth();
  const qc = useQueryClient();
  const query = useQuery({
    queryKey: ["catalog", kind],
    queryFn: async () => {
      if (session?.demo)
        return page<Item>(
          { departments, programs, subjects, semesters, sections }[
            kind
          ] as Item[],
        );
      if (kind === "departments") return api.departments(0, 100);
      if (kind === "programs") return api.programs(0, 100);
      if (kind === "subjects") return api.subjects(0, 100);
      if (kind === "semesters") return api.semesters(0, 100);
      return api.sections(0, 100);
    },
  });
  const status = useMutation({
    mutationFn: async ({ item, active }: { item: Item; active: boolean }) => {
      if (session?.demo) return item;
      if (kind === "departments")
        return api.setDepartmentStatus(item.id, active);
      if (kind === "programs") return api.setProgramStatus(item.id, active);
      if (kind === "subjects") return api.setSubjectStatus(item.id, active);
      if (kind === "semesters") return api.setSemesterStatus(item.id, active);
      return api.setSectionStatus(item.id, active);
    },
    onSuccess: () => void qc.invalidateQueries({ queryKey: ["catalog", kind] }),
  });
  return (
    <>
      <PageHeader
        eyebrow="Academic structure"
        title="Academic catalog"
        description="Build the records that connect programs, teaching assignments, enrollment, attendance, and coursework."
        action={
          <Button onClick={() => setCreating(true)}>
            <Plus size={17} />
            Add {singular[kind]}
          </Button>
        }
      />
      <nav className={styles.catalogNav} aria-label="Academic record types">
        {kinds.map((item) => (
          <NavLink
            key={item}
            to={`/admin/academic/${item}`}
            className={({ isActive }) => (isActive ? styles.active : "")}
          >
            {title(item)}
          </NavLink>
        ))}
      </nav>
      {query.isPending ? (
        <LoadingState />
      ) : query.error ? (
        <ErrorState error={query.error} retry={() => void query.refetch()} />
      ) : query.data.content.length === 0 ? (
        <EmptyState
          title={`No ${kind} yet`}
          description={`Create the first ${singular[kind]} to continue the academic setup sequence.`}
          action={
            <Button onClick={() => setCreating(true)}>
              Create {singular[kind]}
            </Button>
          }
        />
      ) : (
        <Panel
          title={title(kind)}
          description={`${query.data.totalElements} records`}
        >
          <DataTable>
            <thead>
              <tr>
                <th>Name or code</th>
                <th>Academic details</th>
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
                      title={displayName(item)}
                      detail={displayCode(item)}
                    />
                  </td>
                  <td>{details(item)}</td>
                  <td>
                    <StatusBadge value={item.status} />
                  </td>
                  <td>
                    <select
                      className={uiStyles.select}
                      aria-label={`Change status for ${displayName(item)}`}
                      value=""
                      onChange={(event) => {
                        const value = event.target.value;
                        if (value)
                          status.mutate({ item, active: value === "activate" });
                      }}
                    >
                      <option value="" disabled>
                        Change status
                      </option>
                      <option value="activate">Activate</option>
                      <option value="deactivate">Deactivate</option>
                    </select>
                  </td>
                </tr>
              ))}
            </tbody>
          </DataTable>
        </Panel>
      )}
      {creating && (
        <CreateCatalogDialog kind={kind} onClose={() => setCreating(false)} />
      )}
    </>
  );
}
function title(value: string) {
  return value[0].toUpperCase() + value.slice(1);
}
function displayName(item: Item) {
  return "name" in item ? item.name : item.sectionCode;
}
function displayCode(item: Item) {
  return "code" in item
    ? item.code
    : "sectionCode" in item
      ? item.sectionCode
      : "Academic term";
}
function details(item: Item) {
  if ("durationSemesters" in item)
    return `${item.durationSemesters} semesters · ${item.totalCredits} credits`;
  if ("credits" in item) return `${item.credits} credits`;
  if ("startDate" in item) return `${item.startDate} — ${item.endDate}`;
  if ("capacity" in item)
    return `${item.capacity} seats · ${item.schedules.length} schedule blocks`;
  return item.description || "No description";
}

type Form = {
  code: string;
  name: string;
  description: string;
  parentId: string;
  duration: string;
  credits: string;
  startDate: string;
  endDate: string;
  teacherId: string;
  semesterId: string;
  capacity: string;
  day: string;
  startTime: string;
  endTime: string;
};
const empty: Form = {
  code: "",
  name: "",
  description: "",
  parentId: "",
  duration: "8",
  credits: "3",
  startDate: "",
  endDate: "",
  teacherId: "",
  semesterId: "",
  capacity: "30",
  day: "MONDAY",
  startTime: "09:00",
  endTime: "10:30",
};
function CreateCatalogDialog({
  kind,
  onClose,
}: {
  kind: Kind;
  onClose: () => void;
}) {
  const [form, setForm] = useState(empty);
  const api = useServiceApi();
  const { session } = useAuth();
  const qc = useQueryClient();
  const refs = useQuery({
    queryKey: ["catalog", "references"],
    queryFn: async () => {
      if (session?.demo)
        return { departments, programs, subjects, semesters, teachers };
      const [d, p, s, sem, t] = await Promise.all([
        api.departments(0, 100, "ACTIVE"),
        api.programs(0, 100, undefined, "ACTIVE"),
        api.subjects(0, 100, undefined, "ACTIVE"),
        api.semesters(0, 100, "ACTIVE"),
        api.teachers(0, 100, undefined, "ACTIVE"),
      ]);
      return {
        departments: d.content,
        programs: p.content,
        subjects: s.content,
        semesters: sem.content,
        teachers: t.content,
      };
    },
  });
  const mutation = useMutation({
    mutationFn: async () => {
      if (session?.demo) return {};
      if (kind === "departments")
        return api.createDepartment({
          code: form.code,
          name: form.name,
          description: form.description,
        });
      if (kind === "programs")
        return api.createProgram({
          departmentId: form.parentId,
          code: form.code,
          name: form.name,
          durationSemesters: Number(form.duration),
          totalCredits: Number(form.credits),
        });
      if (kind === "subjects")
        return api.createSubject({
          programId: form.parentId,
          code: form.code,
          name: form.name,
          description: form.description,
          credits: Number(form.credits),
          prerequisiteSubjectIds: [],
        });
      if (kind === "semesters")
        return api.createSemester({
          name: form.name,
          startDate: form.startDate,
          endDate: form.endDate,
        });
      return api.createSection({
        subjectId: form.parentId,
        teacherId: form.teacherId,
        semesterId: form.semesterId,
        sectionCode: form.code,
        capacity: Number(form.capacity),
        schedules: [
          {
            dayOfWeek: form.day,
            startTime: form.startTime,
            endTime: form.endTime,
          },
        ],
      });
    },
    onSuccess: () => {
      void qc.invalidateQueries({ queryKey: ["catalog", kind] });
      onClose();
    },
  });
  const set = (key: keyof Form, value: string) =>
    setForm((current) => ({ ...current, [key]: value }));
  const input = (key: keyof Form, label: string, type = "text") => (
    <Field label={label}>
      <input
        required
        type={type}
        value={form[key]}
        onChange={(event) => set(key, event.target.value)}
      />
    </Field>
  );
  return (
    <Dialog
      title={`New ${singular[kind]}`}
      description="Choose related records by name. Internal identifiers remain hidden from the workflow."
      onClose={onClose}
    >
      <form
        onSubmit={(event) => {
          event.preventDefault();
          mutation.mutate();
        }}
      >
        <div className={uiStyles.formGrid}>
          {kind !== "semesters" &&
            input("code", kind === "sections" ? "Section code" : "Code")}
          {kind !== "sections" && input("name", "Name")}
          {kind === "departments" && (
            <Field label="Description" className={uiStyles.span2}>
              <textarea
                value={form.description}
                onChange={(event) => set("description", event.target.value)}
              />
            </Field>
          )}
          {kind === "programs" && (
            <>
              <Reference
                label="Department"
                value={form.parentId}
                onChange={(value) => set("parentId", value)}
                options={refs.data?.departments.map((item) => [
                  item.id,
                  `${item.code} — ${item.name}`,
                ])}
              />
              {input("duration", "Duration in semesters", "number")}
              {input("credits", "Total credits", "number")}
            </>
          )}
          {kind === "subjects" && (
            <>
              <Reference
                label="Program"
                value={form.parentId}
                onChange={(value) => set("parentId", value)}
                options={refs.data?.programs.map((item) => [
                  item.id,
                  `${item.code} — ${item.name}`,
                ])}
              />
              {input("credits", "Credits", "number")}
              <Field label="Description" className={uiStyles.span2}>
                <textarea
                  value={form.description}
                  onChange={(event) => set("description", event.target.value)}
                />
              </Field>
            </>
          )}
          {kind === "semesters" && (
            <>
              {input("startDate", "Start date", "date")}
              {input("endDate", "End date", "date")}
            </>
          )}
          {kind === "sections" && (
            <>
              <Reference
                label="Subject"
                value={form.parentId}
                onChange={(value) => set("parentId", value)}
                options={refs.data?.subjects.map((item) => [
                  item.id,
                  `${item.code} — ${item.name}`,
                ])}
              />
              <Reference
                label="Teacher"
                value={form.teacherId}
                onChange={(value) => set("teacherId", value)}
                options={refs.data?.teachers.map((item) => [
                  item.id,
                  `${item.teacherCode} — ${item.firstName} ${item.lastName}`,
                ])}
              />
              <Reference
                label="Semester"
                value={form.semesterId}
                onChange={(value) => set("semesterId", value)}
                options={refs.data?.semesters.map((item) => [
                  item.id,
                  item.name,
                ])}
              />
              {input("capacity", "Capacity", "number")}
              <Reference
                label="Day"
                value={form.day}
                onChange={(value) => set("day", value)}
                options={[
                  "MONDAY",
                  "TUESDAY",
                  "WEDNESDAY",
                  "THURSDAY",
                  "FRIDAY",
                  "SATURDAY",
                ].map((day) => [day, title(day.toLowerCase())])}
              />
              {input("startTime", "Start time", "time")}
              {input("endTime", "End time", "time")}
            </>
          )}
        </div>
        {mutation.error && (
          <p className={uiStyles.errorText}>{mutation.error.message}</p>
        )}
        <div className={uiStyles.actions}>
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button disabled={mutation.isPending}>
            {mutation.isPending ? "Creating…" : `Create ${singular[kind]}`}
          </Button>
        </div>
      </form>
    </Dialog>
  );
}
function Reference({
  label,
  value,
  onChange,
  options = [],
}: {
  label: string;
  value: string;
  onChange: (value: string) => void;
  options?: string[][];
}) {
  return (
    <Field label={label}>
      <select
        required
        value={value}
        onChange={(event) => onChange(event.target.value)}
      >
        <option value="">Select {label.toLowerCase()}</option>
        {options.map(([id, name]) => (
          <option key={id} value={id}>
            {name}
          </option>
        ))}
      </select>
    </Field>
  );
}
