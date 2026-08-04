export type Role = "ADMIN" | "TEACHER" | "STUDENT";
export type RecordStatus =
  | "ACTIVE"
  | "INACTIVE"
  | "SUSPENDED"
  | "GRADUATED"
  | "PLANNED"
  | "CANCELLED"
  | "DRAFT"
  | "PUBLISHED"
  | "CLOSED"
  | "UPCOMING"
  | "COMPLETED";
export interface PageResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}
export interface AuditFields {
  createdAt: string;
  updatedAt: string;
}
export interface Department extends AuditFields {
  id: string;
  code: string;
  name: string;
  description?: string;
  status: RecordStatus;
}
export interface Program extends AuditFields {
  id: string;
  departmentId: string;
  code: string;
  name: string;
  durationSemesters: number;
  totalCredits: number;
  status: RecordStatus;
}
export interface Subject extends AuditFields {
  id: string;
  departmentId: string;
  programId: string;
  code: string;
  name: string;
  description?: string;
  credits: number;
  prerequisiteSubjectIds: string[];
  status: RecordStatus;
}
export interface Semester extends AuditFields {
  id: string;
  name: string;
  startDate: string;
  endDate: string;
  isRegistrationOpen: boolean;
  status: RecordStatus;
}
export interface Teacher extends AuditFields {
  id: string;
  departmentId: string;
  userId: string;
  teacherCode: string;

  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  hireDate: string;
  status: RecordStatus;
}
export interface Student extends AuditFields {
  id: string;
  userId: string;
  studentCode: string;
  firstName: string;
  lastName: string;
  programId: string;
  gender?: string;
  dateOfBirth: string;
  email: string;
  description?: string;
  credits: number;
  minimumCreditsRequired?: number;
  prerequisiteSubjectIds: string[];
  status: RecordStatus;
}
export interface Schedule {
  id?: string;
  dayOfWeek: string;
  startTime: string;
  endTime: string;
}
export interface Section extends AuditFields {
  id: string;
  subjectId: string;
  teacherId: string;
  semesterId: string;
  sectionCode: string;
  capacity: number;
  schedules: Schedule[];
  status: RecordStatus;
}
export interface TeacherSection {
  id: string;
  sectionCode: string;
  capacity: number;
  schedules: Schedule[];
  status: RecordStatus;
  subject: Pick<Subject, "id" | "code" | "name">;
  semester: Pick<Semester, "id" | "name">;
}
export interface EnrollmentDetail {
  id: string;
  sectionId: string;
  subjectId: string;
  credits: number;
  section: EnrollmentSectionSummary;
  subject: EnrollmentSubjectSummary;
}
export interface EnrollmentSectionSummary {
  id: string;
  sectionCode: string;
}
export interface EnrollmentSubjectSummary {
  id: string;
  code: string;
  name: string;
}
export interface Enrollment extends AuditFields {
  id: string;
  studentId: string;
  semesterId: string;
  semester: Pick<Semester, "id" | "name">;
  status: RecordStatus;
  totalCredits: number;
  details: EnrollmentDetail[];
  isRegistrationOpen: boolean;
  cancelledAt?: string;
}
export interface SectionStudents {
  sectionId: string;
  students: RosterStudent[];
}
export interface RosterStudent {
  studentId: string;
  studentCode: string;
  firstName: string;
  lastName: string;
}
export interface AttendanceSession extends AuditFields {
  id: string;
  sectionId: string;
  sessionNumber: number;
  date: string;
  topic?: string;
}
export interface AttendanceRecord {
  id: string;
  attendanceSessionId: string;
  studentId: string;
  status: "PRESENT" | "ABSENT" | "LATE" | "EXCUSED";
  recordedAt: string;
  updatedAt: string;
}
export interface AttendancePercentage {
  studentId: string;
  sectionId: string;
  presentCount: number;
  totalSessions: number;
  percentage: number;
  eligibleForFinalEvaluation: boolean;
}
export interface Assignment extends AuditFields {
  id: string;
  sectionId: string;
  teacherId: string;
  title: string;
  description?: string;
  dueAt: string;
  maxPoints: number;
  status: RecordStatus;
  publishedAt?: string;
  closedAt?: string;
}
export interface Submission {
  id: string;
  assignmentId: string;
  studentId: string;
  content: string;
  status: string;
  score?: number;
  feedback?: string;
  gradeReleased: boolean;
  submittedAt: string;
  gradedAt?: string;
  gradeReleasedAt?: string;
  updatedAt: string;
}
export interface ProvisioningResponse {
  provisioningId: string;
  userId: string;
  profileId: string;
  role: Role;
  status: string;
  academicCode?: string;
  username?: string;
  universityEmail?: string;
}
export interface AuditRecord {
  eventId: string;
  eventType: string;
  producer: string;
  aggregateType: string;
  aggregateId?: string;
  actorId?: string;
  occurredAt: string;
  traceId?: string;
  payload: string;
}
