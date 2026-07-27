import type {
  Assignment,
  AttendancePercentage,
  AttendanceRecord,
  AttendanceSession,
  Department,
  Enrollment,
  PageResponse,
  Program,
  ProvisioningResponse,
  Section,
  SectionStudents,
  TeacherSection,
  Semester,
  Student,
  Subject,
  Submission,
  Teacher,
} from "./generated/contracts";
import {
  createGatewayClient,
  queryString,
  type TokenProvider,
} from "./gateway-client";

export function createServiceApi(getToken: TokenProvider) {
  const client = createGatewayClient(getToken);
  const academic = "/academic-service/api/v1/academic";
  const student = "/student-service/api/v1/students";
  const enrollment = "/enrollment-service/api/v1/enrollments";
  const attendance = "/attendance-service/api/v1/attendance";
  const assignment = "/assignment-service/api/v1/assignments";
  const list = <T>(
    path: string,
    params: Record<string, string | number | undefined> = {},
  ) => client.get<PageResponse<T>>(`${path}${queryString(params)}`);
  const statusAction = <T>(path: string, id: string, action: string) =>
    client.send<T>(`${path}/${id}/${action}`, "PATCH");
  return {
    departments: (page = 0, size = 20, status?: string) =>
      list<Department>(`${academic}/departments`, { page, size, status }),
    createDepartment: (body: {
      code: string;
      name: string;
      description?: string;
    }) => client.send<Department>(`${academic}/departments`, "POST", body),
    updateDepartment: (id: string, body: { code: string; name: string; description?: string }) => client.send<Department>(`${academic}/departments/${id}`, "PUT", body),
    setDepartmentStatus: (id: string, active: boolean) =>
      statusAction<Department>(
        `${academic}/departments`,
        id,
        active ? "activate" : "deactivate",
      ),
    programs: (page = 0, size = 20, departmentId?: string, status?: string) =>
      list<Program>(`${academic}/programs`, {
        page,
        size,
        departmentId,
        status,
      }),
    createProgram: (body: {
      departmentId: string;
      code: string;
      name: string;
      durationSemesters: number;
      totalCredits: number;
    }) => client.send<Program>(`${academic}/programs`, "POST", body),
    updateProgram: (id: string, body: { departmentId: string; code: string; name: string; durationSemesters: number; totalCredits: number }) => client.send<Program>(`${academic}/programs/${id}`, "PUT", body),
    setProgramStatus: (id: string, active: boolean) =>
      statusAction<Program>(
        `${academic}/programs`,
        id,
        active ? "activate" : "deactivate",
      ),
    semesters: (page = 0, size = 20, status?: string) =>
      list<Semester>(`${academic}/semesters`, { page, size, status }),
    createSemester: (body: {
      name: string;
      startDate: string;
      endDate: string;
    }) => client.send<Semester>(`${academic}/semesters`, "POST", body),
    updateSemester: (id: string, body: { name: string; startDate: string; endDate: string }) => client.send<Semester>(`${academic}/semesters/${id}`, "PUT", body),
    setSemesterStatus: (id: string, active: boolean) =>
      statusAction<Semester>(
        `${academic}/semesters`,
        id,
        active ? "activate" : "deactivate",
      ),
    subjects: (page = 0, size = 20, programId?: string, status?: string) =>
      list<Subject>(`${academic}/subjects`, { page, size, programId, status }),
    createSubject: (body: {
      programId: string;
      code: string;
      name: string;
      description?: string;
      credits: number;
      prerequisiteSubjectIds: string[];
    }) => client.send<Subject>(`${academic}/subjects`, "POST", body),
    updateSubject: (id: string, body: { programId: string; code: string; name: string; description?: string; credits: number; minimumCreditsRequired?: number; prerequisiteSubjectIds: string[] }) => client.send<Subject>(`${academic}/subjects/${id}`, "PUT", body),
    setSubjectStatus: (id: string, active: boolean) =>
      statusAction<Subject>(
        `${academic}/subjects`,
        id,
        active ? "activate" : "deactivate",
      ),
    sections: (
      page = 0,
      size = 20,
      filters: Partial<{
        subjectId: string;
        teacherId: string;
        semesterId: string;
        status: string;
      }> = {},
    ) => list<Section>(`${academic}/sections`, { page, size, ...filters }),
    section: (id: string) => client.get<Section>(`${academic}/sections/${id}`),
    createSection: (body: {
      subjectId: string;
      teacherId: string;
      semesterId: string;
      sectionCode: string;
      capacity: number;
      schedules: { dayOfWeek: string; startTime: string; endTime: string }[];
    }) => client.send<Section>(`${academic}/sections`, "POST", body),
    updateSection: (id: string, body: { subjectId: string; teacherId: string; semesterId: string; sectionCode: string; capacity: number; schedules: { dayOfWeek: string; startTime: string; endTime: string }[] }) => client.send<Section>(`${academic}/sections/${id}`, "PUT", body),
    setSectionStatus: (id: string, active: boolean) =>
      statusAction<Section>(
        `${academic}/sections`,
        id,
        active ? "activate" : "deactivate",
      ),
    teachers: (page = 0, size = 20, departmentId?: string, status?: string) =>
      list<Teacher>(`${academic}/teachers`, {
        page,
        size,
        departmentId,
        status,
      }),
    teacherMe: () => client.get<Teacher>(`${academic}/teachers/me`),
    teacherSections: () =>
      client.get<TeacherSection[]>(`${academic}/teachers/me/sections`),
    setTeacherStatus: (id: string, active: boolean) =>
      statusAction<Teacher>(
        `${academic}/teachers`,
        id,
        active ? "activate" : "deactivate",
      ),
    students: (page = 0, size = 20, programId?: string, status?: string) =>
      list<Student>(student, { page, size, programId, status }),
    studentMe: () => client.get<Student>(`${student}/me`),
    setStudentStatus: (
      id: string,
      action: "activate" | "deactivate" | "suspend",
    ) => statusAction<Student>(student, id, action),
    provisionTeacher: (body: unknown, key = crypto.randomUUID()) =>
      client.send<ProvisioningResponse>(
        "/identity-service/api/v1/provisioning/teachers",
        "POST",
        body,
        { "Idempotency-Key": key },
      ),
    provisionStudent: (body: unknown, key = crypto.randomUUID()) =>
      client.send<ProvisioningResponse>(
        "/identity-service/api/v1/provisioning/students",
        "POST",
        body,
        { "Idempotency-Key": key },
      ),
    enrollments: (
      page = 0,
      size = 20,
      filters: Partial<{
        studentId: string;
        semesterId: string;
        status: string;
      }> = {},
    ) => list<Enrollment>(enrollment, { page, size, ...filters }),
    myEnrollments: (
      page = 0,
      size = 20,
      semesterId?: string,
      status?: string,
    ) =>
      list<Enrollment>(`${enrollment}/me`, { page, size, semesterId, status }),
    createEnrollment: (body: {
      studentId: string;
      semesterId: string;
      sectionIds: string[];
    }) => client.send<Enrollment>(enrollment, "POST", body),
    cancelEnrollment: (id: string) =>
      statusAction<Enrollment>(enrollment, id, "cancel"),
    addEnrollmentSection: (id: string, sectionId: string) =>
      client.send<Enrollment>(`${enrollment}/${id}/sections`, "POST", { sectionId }),
    dropEnrollmentSection: (id: string, sectionId: string) =>
      statusAction<Enrollment>(`${enrollment}/${id}/sections`, sectionId, "drop"),
    sectionStudents: (sectionId: string) =>
      client.get<SectionStudents>(
        `${attendance}/sections/${sectionId}/students`,
      ),
    attendanceSessions: (sectionId: string, page = 0, size = 20) =>
      list<AttendanceSession>(`${attendance}/sessions`, {
        sectionId,
        page,
        size,
      }),
    createAttendanceSession: (body: {
      sectionId: string;
      sessionNumber: number;
      date: string;
      topic?: string;
    }) =>
      client.send<AttendanceSession>(`${attendance}/sessions`, "POST", body),
    attendanceRecords: (sessionId: string, page = 0, size = 100) =>
      list<AttendanceRecord>(`${attendance}/sessions/${sessionId}/records`, {
        page,
        size,
      }),
    recordAttendance: (
      sessionId: string,
      records: { studentId: string; status: string }[],
    ) =>
      client.send<AttendanceRecord[]>(
        `${attendance}/sessions/${sessionId}/records`,
        "POST",
        { records },
      ),
    myAttendance: (sectionId: string) =>
      client.get<AttendancePercentage>(
        `${attendance}/me/sections/${sectionId}/percentage`,
      ),
    assignments: (
      sectionId: string,
      page = 0,
      size = 20,
      status?: string,
      mine = false,
    ) =>
      list<Assignment>(`${assignment}${mine ? "/me" : ""}`, {
        sectionId,
        page,
        size,
        status,
      }),
    createAssignment: (body: {
      sectionId: string;
      title: string;
      description?: string;
      dueAt: string;
      maxPoints: number;
    }) => client.send<Assignment>(assignment, "POST", body),
    publishAssignment: (id: string, teacherId?: string) =>
      client.send<Assignment>(`${assignment}/${id}/publish`, "PATCH", { teacherId }),
    closeAssignment: (id: string, teacherId?: string) =>
      client.send<Assignment>(`${assignment}/${id}/close`, "PATCH", { teacherId }),
    submissions: (assignmentId: string, page = 0, size = 100, mine = false) =>
      list<Submission>(
        `${assignment}/${assignmentId}/submissions${mine ? "/me" : ""}`,
        { page, size },
      ),
    submitAssignment: (assignmentId: string, content: string) =>
      client.send<Submission>(
        `${assignment}/${assignmentId}/submissions/me`,
        "POST",
        { content },
      ),
    gradeSubmission: (id: string, score: number, feedback?: string) =>
      client.send<Submission>(
        `${assignment}/submissions/${id}/grade`,
        "PATCH",
        { score, feedback },
      ),
    releaseGrade: (id: string, teacherId?: string) =>
      client.send<Submission>(
        `${assignment}/submissions/${id}/release-grade`,
        "PATCH",
        { teacherId },
      ),
  };
}
export type ServiceApi = ReturnType<typeof createServiceApi>;
