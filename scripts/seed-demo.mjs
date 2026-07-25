const gatewayUrl = process.env.UMS_GATEWAY_URL ?? "http://localhost:8080";
const keycloakUrl = process.env.UMS_KEYCLOAK_URL ?? "http://localhost:8180";
const adminUsername = process.env.UMS_DEMO_ADMIN_USERNAME ?? "ums.admin";
const adminPassword = process.env.UMS_DEMO_ADMIN_PASSWORD ?? "Admin123!";
const teacherUsername = process.env.UMS_DEMO_TEACHER_USERNAME ?? "demo.teacher";
const teacherPassword = process.env.UMS_DEMO_TEACHER_PASSWORD ?? "Teacher123!";
const studentUsername = process.env.UMS_DEMO_STUDENT_USERNAME ?? "demo.student";
const studentPassword = process.env.UMS_DEMO_STUDENT_PASSWORD ?? "Student123!";

const now = new Date();
const demoYear = now.getUTCFullYear();

function dateAfter(days) {
  const value = new Date(now);
  value.setUTCDate(value.getUTCDate() + days);
  return value.toISOString().slice(0, 10);
}

function dateTimeAfter(days) {
  const value = new Date(now);
  value.setUTCDate(value.getUTCDate() + days);
  return value.toISOString().slice(0, 19);
}

function decodeJwtPayload(token) {
  const payload = token.split(".")[1];
  if (!payload) throw new Error("Keycloak returned an invalid access token");
  return JSON.parse(Buffer.from(payload, "base64url").toString("utf8"));
}

async function readResponse(response) {
  const text = await response.text();
  if (!text) return null;
  try {
    return JSON.parse(text);
  } catch {
    return text;
  }
}

async function request(url, options = {}) {
  const retryUnavailable = options.retryUnavailable === true;
  const fetchOptions = { ...options };
  delete fetchOptions.retryUnavailable;
  for (let attempt = 0; attempt < (retryUnavailable ? 16 : 1); attempt += 1) {
    let response;
    try {
      response = await fetch(url, fetchOptions);
    } catch (error) {
      if (retryUnavailable && attempt < 15) {
        await new Promise((resolve) => setTimeout(resolve, 2000));
        continue;
      }
      throw new Error(`Cannot reach ${url}: ${error.message}`);
    }
    const body = await readResponse(response);
    if (response.ok) return body;
    if (retryUnavailable && [500, 502, 503, 504].includes(response.status) && attempt < 15) {
      await new Promise((resolve) => setTimeout(resolve, 2000));
      continue;
    }
    const details = typeof body === "string" ? body : JSON.stringify(body);
    throw new Error(`${fetchOptions.method ?? "GET"} ${url} returned ${response.status}: ${details}`);
  }
  throw new Error(`GET ${url} remained unavailable after waiting for service discovery`);
}

async function obtainAdminToken() {
  const form = new URLSearchParams({
    client_id: "ums-web",
    grant_type: "password",
    username: adminUsername,
    password: adminPassword,
  });
  const token = await request(`${keycloakUrl}/realms/ums/protocol/openid-connect/token`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: form,
  });
  const claims = decodeJwtPayload(token.access_token);
  const roles = claims.realm_access?.roles ?? [];
  const audiences = Array.isArray(claims.aud) ? claims.aud : [claims.aud];
  if (!roles.includes("ADMIN") || !audiences.includes("ums-api")) {
    throw new Error("The development administrator token is missing the ADMIN role or ums-api audience");
  }
  return token.access_token;
}

async function main() {
  console.log("Connecting to the local development realm...");
  const accessToken = await obtainAdminToken();

  async function api(path, { method = "GET", body, headers = {} } = {}) {
    return request(`${gatewayUrl}${path}`, {
      method,
      retryUnavailable: method === "GET",
      headers: {
        Authorization: `Bearer ${accessToken}`,
        ...(body === undefined ? {} : { "Content-Type": "application/json" }),
        ...headers,
      },
      body: body === undefined ? undefined : JSON.stringify(body),
    });
  }

  async function findOrCreate({ label, listPath, matches, createPath, body }) {
    const page = await api(listPath);
    const existing = page.content?.find(matches);
    if (existing) {
      console.log(`[reuse] ${label}: ${existing.id}`);
      return existing;
    }
    const created = await api(createPath, { method: "POST", body });
    console.log(`[create] ${label}: ${created.id}`);
    return created;
  }

  const department = await findOrCreate({
    label: "department",
    listPath: "/academic-service/api/v1/academic/departments?page=0&size=100",
    matches: (value) => value.code === "DEMO-CS",
    createPath: "/academic-service/api/v1/academic/departments",
    body: {
      code: "DEMO-CS",
      name: "Demo Computer Science",
      description: "Development-only academic data",
    },
  });

  const program = await findOrCreate({
    label: "program",
    listPath: `/academic-service/api/v1/academic/programs?departmentId=${department.id}&page=0&size=100`,
    matches: (value) => value.code === "DEMO-SE",
    createPath: "/academic-service/api/v1/academic/programs",
    body: {
      departmentId: department.id,
      code: "DEMO-SE",
      name: "Demo Software Engineering",
      durationSemesters: 8,
      totalCredits: 160,
    },
  });

  const teacherProvisioning = await api("/identity-service/api/v1/provisioning/teachers", {
    method: "POST",
    headers: { "Idempotency-Key": "demo-teacher-v1" },
    body: {
      username: teacherUsername,
      temporaryPassword: teacherPassword,
      departmentId: department.id,
      teacherCode: "T-DEMO-001",
      firstName: "Ada",
      lastName: "Lovelace",
      email: "ada.demo@ums.local",
      phone: "+51-900-100-001",
      hireDate: `${demoYear}-01-15`,
    },
  });
  console.log(`[reuse/create] teacher: ${teacherProvisioning.profileId}`);

  const studentProvisioning = await api("/identity-service/api/v1/provisioning/students", {
    method: "POST",
    headers: { "Idempotency-Key": "demo-student-v1" },
    body: {
      username: studentUsername,
      temporaryPassword: studentPassword,
      studentCode: "S-DEMO-001",
      firstName: "Grace",
      lastName: "Hopper",
      gender: "FEMALE",
      dateOfBirth: "2001-12-09",
      email: "grace.demo@ums.local",
      phone: "+51-900-100-002",
      address: "Lima, Peru",
      programId: program.id,
      admissionDate: dateAfter(0),
    },
  });
  console.log(`[reuse/create] student: ${studentProvisioning.profileId}`);

  let semester = await findOrCreate({
    label: "semester",
    listPath: "/academic-service/api/v1/academic/semesters?page=0&size=100",
    matches: (value) => value.name === `Demo Semester ${demoYear}`,
    createPath: "/academic-service/api/v1/academic/semesters",
    body: {
      name: `Demo Semester ${demoYear}`,
      startDate: dateAfter(0),
      endDate: dateAfter(120),
    },
  });
  if (semester.status !== "ACTIVE") {
    semester = await api(`/academic-service/api/v1/academic/semesters/${semester.id}/activate`, {
      method: "PATCH",
    });
    console.log(`[activate] semester: ${semester.id}`);
  }

  const subject = await findOrCreate({
    label: "subject",
    listPath: `/academic-service/api/v1/academic/subjects?programId=${program.id}&page=0&size=100`,
    matches: (value) => value.code === "DEMO-MS-101",
    createPath: "/academic-service/api/v1/academic/subjects",
    body: {
      programId: program.id,
      code: "DEMO-MS-101",
      name: "Microservices Architecture",
      description: "Spring Boot and distributed systems",
      credits: 4,
      minimumCreditsRequired: 0,
      prerequisiteSubjectIds: [],
    },
  });

  const section = await findOrCreate({
    label: "section",
    listPath: `/academic-service/api/v1/academic/sections?subjectId=${subject.id}&semesterId=${semester.id}&page=0&size=100`,
    matches: (value) => value.sectionCode === "DEMO-MS-A",
    createPath: "/academic-service/api/v1/academic/sections",
    body: {
      subjectId: subject.id,
      teacherId: teacherProvisioning.profileId,
      semesterId: semester.id,
      sectionCode: "DEMO-MS-A",
      capacity: 30,
      schedules: [
        {
          dayOfWeek: "MONDAY",
          startTime: "09:00:00",
          endTime: "11:00:00",
        },
      ],
    },
  });

  const enrollmentPage = await api(
    `/enrollment-service/api/v1/enrollments?studentId=${studentProvisioning.profileId}&semesterId=${semester.id}&page=0&size=100`,
  );
  let enrollment = enrollmentPage.content?.find((value) => value.status === "ACTIVE");
  if (enrollment) {
    console.log(`[reuse] enrollment: ${enrollment.id}`);
  } else {
    enrollment = await api("/enrollment-service/api/v1/enrollments", {
      method: "POST",
      body: {
        studentId: studentProvisioning.profileId,
        semesterId: semester.id,
        sectionIds: [section.id],
      },
    });
    console.log(`[create] enrollment: ${enrollment.id}`);
  }

  const session = await findOrCreate({
    label: "attendance session",
    listPath: `/attendance-service/api/v1/attendance/sessions?sectionId=${section.id}&page=0&size=100`,
    matches: (value) => value.sessionNumber === 1,
    createPath: "/attendance-service/api/v1/attendance/sessions",
    body: {
      sectionId: section.id,
      sessionNumber: 1,
      date: dateAfter(0),
      topic: "Introduction to microservices",
    },
  });
  await api(`/attendance-service/api/v1/attendance/sessions/${session.id}/records`, {
    method: "POST",
    body: {
      records: [{ studentId: studentProvisioning.profileId, status: "PRESENT" }],
    },
  });
  console.log(`[upsert] attendance record for student: ${studentProvisioning.profileId}`);

  let assignment = await findOrCreate({
    label: "assignment",
    listPath: `/assignment-service/api/v1/assignments?sectionId=${section.id}&page=0&size=100`,
    matches: (value) => value.title === "Build a University Microservice",
    createPath: "/assignment-service/api/v1/assignments",
    body: {
      sectionId: section.id,
      teacherId: teacherProvisioning.profileId,
      title: "Build a University Microservice",
      description: "Implement one bounded context and document its API.",
      dueAt: dateTimeAfter(30),
      maxPoints: 100,
    },
  });
  if (assignment.status === "DRAFT") {
    assignment = await api(`/assignment-service/api/v1/assignments/${assignment.id}/publish`, {
      method: "PATCH",
      body: { teacherId: teacherProvisioning.profileId },
    });
    console.log(`[publish] assignment: ${assignment.id}`);
  }

  const submissionPage = await api(
    `/assignment-service/api/v1/assignments/${assignment.id}/submissions?studentId=${studentProvisioning.profileId}&page=0&size=100`,
  );
  let submission = submissionPage.content?.find(
    (value) => value.studentId === studentProvisioning.profileId,
  );
  if (submission) {
    console.log(`[reuse] submission: ${submission.id}`);
  } else {
    submission = await api(`/assignment-service/api/v1/assignments/${assignment.id}/submissions`, {
      method: "POST",
      body: {
        studentId: studentProvisioning.profileId,
        content: "Demo submission created through the public API.",
      },
    });
    console.log(`[create] submission: ${submission.id}`);
  }
  if (!submission.gradeReleased) {
    await api(`/assignment-service/api/v1/assignments/submissions/${submission.id}/grade`, {
      method: "PATCH",
      body: {
        teacherId: teacherProvisioning.profileId,
        score: 92.5,
        feedback: "Clear architecture and good tests.",
      },
    });
    submission = await api(
      `/assignment-service/api/v1/assignments/submissions/${submission.id}/release-grade`,
      {
        method: "PATCH",
        body: { teacherId: teacherProvisioning.profileId },
      },
    );
    console.log(`[grade/release] submission: ${submission.id}`);
  }

  console.log("\nDemo data is ready.");
  console.log(`Administrator: ${adminUsername} / ${adminPassword}`);
  console.log(`Teacher: ${teacherUsername} / ${teacherPassword} (password change required at first login)`);
  console.log(`Student: ${studentUsername} / ${studentPassword} (password change required at first login)`);
  console.log("Re-running this command reuses the same demo records.");
}

main().catch((error) => {
  console.error(`\nDemo seeding failed: ${error.message}`);
  console.error("Start the complete development stack with the dev profile, then retry.");
  process.exitCode = 1;
});
