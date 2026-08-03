import { useRef, useState } from "react";
import { zodResolver } from "@hookform/resolvers/zod";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useForm } from "react-hook-form";
import { Plus, UserCheck } from "lucide-react";
import { z } from "zod";
import { useParams } from "react-router-dom";
import type {
  PageResponse,
  Student,
  Teacher,
} from "../../../api/generated/contracts";
import { useServiceApi } from "../../../api/use-service-api";
import { useAuth } from "../../../auth/AuthProvider";
import { Link } from "react-router-dom";
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

const schema = z.object({
  firstName: z.string().min(2, "Enter a first name"),
  lastName: z.string().min(2, "Enter a last name"),
  contactEmail: z.email("Enter a valid personal email"),
  organizationId: z.string().min(1, "Choose an academic unit"),
  primaryDate: z.string().min(1, "Choose a date"),
  secondaryDate: z.string().optional(),
  phone: z.string().optional(),
  gender: z.string().optional(),
  address: z.string().optional(),
});
type FormValues = z.infer<typeof schema>;
export function PeoplePage() {
  const kind = useParams().kind === "teachers" ? "teacher" : "student";
  const [creating, setCreating] = useState(false);
  const [statusFilter, setStatusFilter] = useState<string>("");
  const api = useServiceApi();
  const { session } = useAuth();
  const qc = useQueryClient();
  const query = useQuery<PageResponse<Student | Teacher>>({
    queryKey: ["people", kind, statusFilter],
    queryFn: async () => {
      return kind === "student"
        ? await api.students(0, 100, undefined, statusFilter || undefined)
        : await api.teachers(0, 100, undefined, statusFilter || undefined);
    },
  });
  const changeStatus = useMutation({
    mutationFn: async ({ id, action }: { id: string; action: string }) => {
      return kind === "student"
        ? api.setStudentStatus(
            id,
            action as "activate" | "deactivate" | "suspend",
          )
        : api.setTeacherStatus(id, action === "activate");
    },
    onSuccess: () => void qc.invalidateQueries({ queryKey: ["people", kind] }),
  });
  const label = kind === "student" ? "Students" : "Teachers";
  return (
    <>
      <PageHeader
        eyebrow="People and access"
        title={label}
        description={`Create each ${kind}'s university account and academic profile in one reliable workflow.`}
        action={
          <Button onClick={() => setCreating(true)}>
            <Plus size={17} />
            Create {kind}
          </Button>
        }
      />
      {query.isPending ? (
        <LoadingState />
      ) : query.error ? (
        <ErrorState error={query.error} retry={() => void query.refetch()} />
      ) : query.data.content.length === 0 ? (
        <EmptyState
          title={`No ${label.toLowerCase()} yet`}
          description={`Add the first ${kind} to automatically generate their credentials and university profile.`}
          action={
            <Button onClick={() => setCreating(true)}>Create {kind}</Button>
          }
        />
      ) : (
        <Panel
          title={label}
          description={`${query.data.totalElements} academic profiles`}
          action={
            <select
              className={uiStyles.select}
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="">All statuses</option>
              <option value="ACTIVE">Active</option>
              <option value="INACTIVE">Inactive</option>
              <option value="SUSPENDED">Suspended</option>
            </select>
          }
        >
          <DataTable>
            <thead>
              <tr>
                <th>Person</th>
                <th>Academic code</th>
                <th>Academic unit</th>
                <th>Status</th>
                <th>
                  <span className="sr-only">Actions</span>
                </th>
              </tr>
            </thead>
            <tbody>
              {query.data.content.map((person) => (
                <tr key={person.id}>
                  <td>
                    <PrimaryCell
                      title={`${person.firstName} ${person.lastName}`}
                      detail={person.email}
                    />
                  </td>
                  <td>
                    {"studentCode" in person
                      ? person.studentCode
                      : person.teacherCode}
                  </td>
                  <td>
                    {shortId(
                      "programId" in person
                        ? person.programId
                        : person.departmentId,
                    )}
                  </td>
                  <td>
                    <StatusBadge value={person.status} />
                  </td>
                  <td>
                    <div style={{ display: 'flex', gap: '0.5rem', alignItems: 'center' }}>
                      <Link
                        to={`/admin/people/${kind === "student" ? "students" : "teachers"}/${person.id}`}
                        className={uiStyles.button}
                        style={{ padding: "0.25rem 0.5rem", height: "auto" }}
                      >
                        Profile
                      </Link>
                      <select
                        className={uiStyles.select}
                        value=""
                        aria-label={`Change status for ${person.firstName} ${person.lastName}`}
                        onChange={(event) => {
                          if (event.target.value) {
                            changeStatus.mutate({
                              id: person.id,
                              action: event.target.value,
                            });
                          }
                        }}
                      >
                        <option value="" disabled>
                          Change status
                        </option>
                        <option value="activate">Activate</option>
                        <option value="deactivate">Deactivate</option>
                        {kind === "student" && (
                          <option value="suspend">Suspend</option>
                        )}
                      </select>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </DataTable>
        </Panel>
      )}
      {creating && (
        <ProvisionDialog kind={kind} onClose={() => setCreating(false)} />
      )}
    </>
  );
}
function shortId(value: string) {
  return value.length > 12 ? `${value.slice(0, 8)}…` : value;
}
function ProvisionDialog({
  kind,
  onClose,
}: {
  kind: "student" | "teacher";
  onClose: () => void;
}) {
  const api = useServiceApi();
  const { session } = useAuth();
  const qc = useQueryClient();
  const key = useRef(crypto.randomUUID());
  const units = useQuery({
    queryKey: ["provision", kind, "units"],
    queryFn: async () => {
      return kind === "student"
        ? (await api.programs(0, 100, undefined, "ACTIVE")).content
        : (await api.departments(0, 100, "ACTIVE")).content;
    },
  });
  const form = useForm<FormValues>({
    resolver: zodResolver(schema),
    defaultValues: {
      firstName: "",
      lastName: "",
      contactEmail: "",
      organizationId: "",
      primaryDate: "",
      secondaryDate: "",
      phone: "",
      gender: "",
      address: "",
    },
  });
  const mutation = useMutation({
    mutationFn: async (value: FormValues) => {
      const body =
        kind === "student"
          ? {
              contactEmail: value.contactEmail,
              firstName: value.firstName,
              lastName: value.lastName,
              dateOfBirth: value.primaryDate,
              programId: value.organizationId,
              admissionDate: value.secondaryDate,
              gender: value.gender || undefined,
              phone: value.phone || undefined,
              address: value.address || undefined,
            }
          : {
              contactEmail: value.contactEmail,
              firstName: value.firstName,
              lastName: value.lastName,
              departmentId: value.organizationId,
              hireDate: value.primaryDate,
              phone: value.phone || undefined,
            };
      return kind === "student"
        ? api.provisionStudent(body, key.current)
        : api.provisionTeacher(body, key.current);
    },
    onSuccess: () => void qc.invalidateQueries({ queryKey: ["people", kind] }),
  });
  if (mutation.data)
    return (
      <Dialog
        title={`${kind === "student" ? "Student" : "Teacher"} created`}
        description="The account and academic profile were provisioned together."
        onClose={onClose}
      >
        <EmptyState
          title="Ready to sign in"
          description={`Code: ${mutation.data.academicCode ?? "generated"} · Username: ${mutation.data.username ?? "generated"} · University email: ${mutation.data.universityEmail ?? "generated"}. A password-set invitation was sent to the personal contact address.`}
          action={
            <Button onClick={onClose}>
              <UserCheck size={17} />
              Return to {kind}s
            </Button>
          }
        />
      </Dialog>
    );
  const {
    register,
    handleSubmit,
    formState: { errors },
  } = form;
  return (
    <Dialog
      title={`Create ${kind}`}
      description="Enter the details below to generate the academic profile and login credentials."
      onClose={onClose}
    >
      <form onSubmit={handleSubmit((values) => mutation.mutate(values))}>
        <div className={uiStyles.formGrid}>
          <Field label="First name" error={errors.firstName?.message}>
            <input {...register("firstName")} />
          </Field>
          <Field label="Last name" error={errors.lastName?.message}>
            <input {...register("lastName")} />
          </Field>
          <Field label="Personal contact email" error={errors.contactEmail?.message}>
            <input type="email" {...register("contactEmail")} />
          </Field>
          <Field
            label={kind === "student" ? "Program" : "Department"}
            error={errors.organizationId?.message}
          >
            <select {...register("organizationId")}>
              <option value="">Select an academic unit</option>
              {units.data?.map((unit) => (
                <option key={unit.id} value={unit.id}>
                  {unit.code} — {unit.name}
                </option>
              ))}
            </select>
          </Field>
          <Field
            label={kind === "student" ? "Date of birth" : "Hire date"}
            error={errors.primaryDate?.message}
          >
            <input type="date" {...register("primaryDate")} />
          </Field>
          {kind === "student" && (
            <Field label="Admission date" error={errors.secondaryDate?.message}>
              <input type="date" {...register("secondaryDate")} />
            </Field>
          )}
          <Field label="Phone">
            <input {...register("phone")} />
          </Field>
          {kind === "student" && (
            <Field label="Gender">
              <select {...register("gender")}>
                <option value="">Not specified</option>
                <option value="FEMALE">Female</option>
                <option value="MALE">Male</option>
                <option value="OTHER">Other</option>
              </select>
            </Field>
          )}
          {kind === "student" && (
            <Field label="Address" className={uiStyles.span2}>
              <input {...register("address")} />
            </Field>
          )}
        </div>
        <p style={{ marginTop: 15 }} className={uiStyles.muted}>
          The system creates the academic code, username, university email, and a secure password-set invitation.
        </p>
        {mutation.error && (
          <p className={uiStyles.errorText}>{mutation.error.message}</p>
        )}
        <div className={uiStyles.actions}>
          <Button type="button" variant="secondary" onClick={onClose}>
            Cancel
          </Button>
          <Button disabled={mutation.isPending}>
            {mutation.isPending
              ? "Creating account and profile…"
              : `Create ${kind}`}
          </Button>
        </div>
      </form>
    </Dialog>
  );
}
