import type {
  Assignment,
  AttendanceSession,
  Department,
  Enrollment,
  PageResponse,
  Program,
  Section,
  Semester,
  Student,
  Subject,
  Submission,
  Teacher,
  TeacherSection,
} from "../api/generated/contracts";
const now = "2026-10-14T15:00:00Z";
export const departments: Department[] = [
  {
    id: "d1",
    code: "CS",
    name: "Computer Science",
    description: "Computing and information systems",
    status: "ACTIVE",
    createdAt: now,
    updatedAt: now,
  },
  {
    id: "d2",
    code: "MATH",
    name: "Mathematics",
    description: "Pure and applied mathematics",
    status: "ACTIVE",
    createdAt: now,
    updatedAt: now,
  },
];
export const programs: Program[] = [
  {
    id: "p1",
    departmentId: "d1",
    code: "BSCS",
    name: "Computer Science",
    durationSemesters: 8,
    totalCredits: 160,
    status: "ACTIVE",
    createdAt: now,
    updatedAt: now,
  },
];
export const teachers: Teacher[] = [
  {
    id: "t1",
    departmentId: "d1",
    userId: "u1",
    teacherCode: "T-0184",
    firstName: "Marcus",
    lastName: "Lee",
    email: "marcus.lee@university.edu",
    hireDate: "2022-03-14",
    status: "ACTIVE",
    createdAt: now,
    updatedAt: now,
  },
];
export const students: Student[] = [
  {
    id: "s1",
    userId: "u2",
    studentCode: "S-10482",
    firstName: "Sofia",
    lastName: "Chen",
    dateOfBirth: "2004-06-18",
    email: "sofia.chen@university.edu",
    programId: "p1",
    admissionDate: "2024-03-01",
    status: "ACTIVE",
    createdAt: now,
    updatedAt: now,
  },
  {
    id: "s2",
    userId: "u3",
    studentCode: "S-10479",
    firstName: "Liam",
    lastName: "Johnson",
    dateOfBirth: "2003-11-08",
    email: "liam.j@university.edu",
    programId: "p1",
    admissionDate: "2023-03-01",
    status: "ACTIVE",
    createdAt: now,
    updatedAt: now,
  },
];
export const semesters: Semester[] = [
  {
    id: "sem1",
    name: "Fall 2026",
    startDate: "2026-08-17",
    endDate: "2026-12-18",
    status: "ACTIVE",
    createdAt: now,
    updatedAt: now,
  },
];
export const subjects: Subject[] = [
  {
    id: "sub1",
    programId: "p1",
    code: "CS-402",
    name: "Distributed Systems",
    description: "Distributed computing principles",
    credits: 4,
    prerequisiteSubjectIds: [],
    status: "ACTIVE",
    createdAt: now,
    updatedAt: now,
  },
];
export const sections: Section[] = [
  {
    id: "sec1",
    subjectId: "sub1",
    teacherId: "t1",
    semesterId: "sem1",
    sectionCode: "CS402-A",
    capacity: 28,
    schedules: [
      { dayOfWeek: "MONDAY", startTime: "10:30", endTime: "11:50" },
      { dayOfWeek: "WEDNESDAY", startTime: "10:30", endTime: "11:50" },
    ],
    status: "ACTIVE",
    createdAt: now,
    updatedAt: now,
  },
];
export const teacherSections: TeacherSection[] = sections.map((section) => {
  const subject = subjects.find((item) => item.id === section.subjectId)!;
  const semester = semesters.find((item) => item.id === section.semesterId)!;
  return {
    id: section.id,
    sectionCode: section.sectionCode,
    capacity: section.capacity,
    schedules: section.schedules,
    status: section.status,
    subject: { id: subject.id, code: subject.code, name: subject.name },
    semester: { id: semester.id, name: semester.name },
  };
});
export const enrollments: Enrollment[] = [
  {
    id: "e1",
    studentId: "s1",
    semesterId: "sem1",
    status: "ACTIVE",
    totalCredits: 4,
    semester: { id: "sem1", name: "Fall 2026" },
    details: [{
      id: "ed1",
      sectionId: "sec1",
      subjectId: "sub1",
      credits: 4,
      section: { id: "sec1", sectionCode: "CS402-A" },
      subject: { id: "sub1", code: "CS-402", name: "Distributed Systems" },
    }],
    createdAt: now,
    updatedAt: now,
  },
];
export const assignments: Assignment[] = [
  {
    id: "a1",
    sectionId: "sec1",
    teacherId: "t1",
    title: "Architecture review",
    description: "Review a distributed system architecture.",
    dueAt: "2026-10-16T23:59:00",
    maxPoints: 100,
    status: "PUBLISHED",
    createdAt: now,
    updatedAt: now,
    publishedAt: now,
  },
];
export const submissions: Submission[] = [
  {
    id: "submission-1",
    assignmentId: "a1",
    studentId: "s1",
    content:
      "Architecture analysis with consistency and availability trade-offs.",
    status: "SUBMITTED",
    gradeReleased: false,
    submittedAt: now,
    updatedAt: now,
  },
];
export const attendanceSessions: AttendanceSession[] = [
  {
    id: "as1",
    sectionId: "sec1",
    sessionNumber: 8,
    date: "2026-10-14",
    topic: "Consensus algorithms",
    createdAt: now,
    updatedAt: now,
  },
];
export function page<T>(
  content: T[],
  pageNumber = 0,
  size = 20,
): PageResponse<T> {
  return {
    content,
    page: pageNumber,
    size,
    totalElements: content.length,
    totalPages: content.length ? 1 : 0,
  };
}
