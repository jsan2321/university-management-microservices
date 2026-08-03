# API Reference & Walkthrough

## Access model

Use the API Gateway as the supported browser and API-client entry point:

```text
http://localhost:8080/{service-name}/api/...
```

Requests use a Keycloak bearer token. The gateway enforces realm roles before forwarding requests. Internal service-to-service routes under `/internal/**` are never public API endpoints.

For an automated development dataset, run `node scripts/seed-demo.mjs` from the repository root.

## Gateway routes

| Service | Gateway prefix | API domain | Typical access |
| --- | --- | --- | --- |
| Student | `/student-service` | `/api/v1/students` | Administrator; `/me` is student-only |
| Academic | `/academic-service` | `/api/v1/academic` | Catalog reads for authenticated roles; changes for administrators |
| Enrollment | `/enrollment-service` | `/api/v1/enrollments` | Reads for authenticated roles; changes for administrators |
| Attendance | `/attendance-service` | `/api/v1/attendance` | Reads for authenticated roles; recording for administrators and teachers |
| Assignment | `/assignment-service` | `/api/v1/assignments` | Reads for authenticated roles; authoring for administrators and teachers; submissions for students |
| Identity | `/identity-service` | `/api/v1/provisioning` | Administrator-only |
| Audit | `/audit-service` | `/api/audits` | Administrator-only |

For example, the student profile endpoint is available through `http://localhost:8080/student-service/api/v1/students/me`.

## Endpoint domains

- **Students:** profile retrieval, status lifecycle, and signed-in student profile resolution.
- **Academic catalog:** departments, programs, teachers, semesters, subjects, and sections.
- **Enrollments:** create, list, retrieve, cancel, and signed-in student enrollment views.
- **Attendance:** sessions, attendance records, section rosters, and percentage views.
- **Assignments:** assignment lifecycle, submissions, grading, and grade release.
- **Identity provisioning:** create and link Keycloak teacher and student accounts with domain profiles.
- **System audit:** paginated and filterable record history of asynchronous business events published via Kafka.

## Interactive OpenAPI documentation

Each business service exposes local Swagger UI for development and endpoint-level schemas. Swagger is intentionally accessed directly from the service port; it is not the gateway client contract.

| Service | Swagger UI | OpenAPI JSON |
| --- | --- | --- |
| Student | `http://localhost:8081/swagger-ui/index.html` | `http://localhost:8081/v3/api-docs` |
| Academic | `http://localhost:8082/swagger-ui/index.html` | `http://localhost:8082/v3/api-docs` |
| Enrollment | `http://localhost:8083/swagger-ui/index.html` | `http://localhost:8083/v3/api-docs` |
| Attendance | `http://localhost:8084/swagger-ui/index.html` | `http://localhost:8084/v3/api-docs` |
| Assignment | `http://localhost:8085/swagger-ui/index.html` | `http://localhost:8085/v3/api-docs` |
| Identity | `http://localhost:8086/swagger-ui/index.html` | `http://localhost:8086/v3/api-docs` |
| Audit | `http://localhost:8087/swagger-ui/index.html` | `http://localhost:8087/v3/api-docs` |

The documentation pages and OpenAPI JSON are publicly readable in local development. Select **Authorize** in Swagger UI and enter a valid bearer token before calling protected business endpoints.

---

## API Walkthrough

This walkthrough creates a small university dataset through the secured API Gateway. It uses generated IDs from earlier responses; do not copy fixed database identifiers from old test notes. The seeder and this walkthrough use only public APIs. They never insert records directly into service databases.

### 1. Start the development environment

Start PostgreSQL, Keycloak, Config Server, Eureka, the gateway, and all six business services as described in the root README.

The development realm includes:

```text
Username: ums.admin
Password: Admin123!
Realm role: ADMIN
```

This is a local-development identity, not the separate `master`-realm Keycloak bootstrap administrator.

### 2. Obtain an administrator token

Request a token from the development-only `ums-web` client:

```sh
curl --request POST "http://localhost:8180/realms/ums/protocol/openid-connect/token" \
  --header "Content-Type: application/x-www-form-urlencoded" \
  --data-urlencode "client_id=ums-web" \
  --data-urlencode "grant_type=password" \
  --data-urlencode "username=ums.admin" \
  --data-urlencode "password=Admin123!"
```

Copy the returned `access_token`. In Swagger UI, select **Authorize** and enter the token. For other clients, send:

```text
Authorization: Bearer <access-token>
Content-Type: application/json
```

### 3. Create the academic foundation

Use the gateway URL `http://localhost:8080`.

Create a department:

```http
POST /academic-service/api/v1/academic/departments
```

```json
{
  "code": "DEMO-CS",
  "name": "Demo Computer Science",
  "description": "Development-only academic data"
}
```

Save its `id` as `<department-id>`, then create a program:

```http
POST /academic-service/api/v1/academic/programs
```

```json
{
  "departmentId": "<department-id>",
  "code": "DEMO-SE",
  "name": "Demo Software Engineering",
  "durationSemesters": 8,
  "totalCredits": 160
}
```

Save the program `id` as `<program-id>`.

### 4. Provision teacher and student identities

Provisioning calls require a unique `Idempotency-Key` header. Repeating the same request with the same key returns the completed provisioning result.

```http
POST /identity-service/api/v1/provisioning/teachers
Idempotency-Key: walkthrough-teacher-v1
```

```json
{
  "username": "walkthrough.teacher",
  "temporaryPassword": "Teacher123!",
  "departmentId": "<department-id>",
  "teacherCode": "T-WALK-001",
  "firstName": "Ada",
  "lastName": "Lovelace",
  "email": "ada.walkthrough@ums.local",
  "phone": "+51-900-100-001",
  "hireDate": "2025-01-15"
}
```

Save `profileId` as `<teacher-id>`.

```http
POST /identity-service/api/v1/provisioning/students
Idempotency-Key: walkthrough-student-v1
```

```json
{
  "username": "walkthrough.student",
  "temporaryPassword": "Student123!",
  "studentCode": "S-WALK-001",
  "firstName": "Grace",
  "lastName": "Hopper",
  "gender": "FEMALE",
  "dateOfBirth": "2001-12-09",
  "email": "grace.walkthrough@ums.local",
  "phone": "+51-900-100-002",
  "address": "Lima, Peru",
  "programId": "<program-id>",
  "admissionDate": "2025-03-01"
}
```

Save `profileId` as `<student-id>`. Both users must change their temporary password during first interactive login.

### 5. Create a semester, subject, and section

Create a semester whose end date is in the future, save `<semester-id>`, and activate it. Replace `<today>` and `<future-date>` with ISO dates such as `2030-03-01` and `2030-07-15`:

```http
POST  /academic-service/api/v1/academic/semesters
PATCH /academic-service/api/v1/academic/semesters/<semester-id>/activate
```

```json
{
  "name": "Walkthrough Semester",
  "startDate": "<today>",
  "endDate": "<future-date>"
}
```

Create a subject and save `<subject-id>`:

```http
POST /academic-service/api/v1/academic/subjects
```

```json
{
  "programId": "<program-id>",
  "code": "WALK-MS-101",
  "name": "Microservices Architecture",
  "description": "Spring Boot and distributed systems",
  "credits": 4,
  "minimumCreditsRequired": 0,
  "prerequisiteSubjectIds": []
}
```

Create a section and save `<section-id>`:

```http
POST /academic-service/api/v1/academic/sections
```

```json
{
  "subjectId": "<subject-id>",
  "teacherId": "<teacher-id>",
  "semesterId": "<semester-id>",
  "sectionCode": "WALK-MS-A",
  "capacity": 30,
  "schedules": [
    {
      "dayOfWeek": "MONDAY",
      "startTime": "09:00:00",
      "endTime": "11:00:00"
    }
  ]
}
```

### 6. Enroll the student

```http
POST /enrollment-service/api/v1/enrollments
```

```json
{
  "studentId": "<student-id>",
  "semesterId": "<semester-id>",
  "sectionIds": ["<section-id>"]
}
```

Save the returned enrollment `id`. Keep it active for attendance and assignment testing.

### 7. Record attendance

Create a session with the current ISO date and save `<session-id>`:

```http
POST /attendance-service/api/v1/attendance/sessions
```

```json
{
  "sectionId": "<section-id>",
  "sessionNumber": 1,
  "date": "<today>",
  "topic": "Introduction to microservices"
}
```

Record attendance:

```http
POST /attendance-service/api/v1/attendance/sessions/<session-id>/records
```

```json
{
  "records": [
    {
      "studentId": "<student-id>",
      "status": "PRESENT"
    }
  ]
}
```

### 8. Complete the assignment workflow

Create an assignment with a future `dueAt`, save `<assignment-id>`, then publish it. Replace `<future-date-time>` with an ISO local date-time such as `2030-04-15T23:59:00`:

```http
POST  /assignment-service/api/v1/assignments
PATCH /assignment-service/api/v1/assignments/<assignment-id>/publish
```

```json
{
  "sectionId": "<section-id>",
  "teacherId": "<teacher-id>",
  "title": "Build a University Microservice",
  "description": "Implement one bounded context and document its API.",
  "dueAt": "<future-date-time>",
  "maxPoints": 100
}
```

The publish request body is:

```json
{
  "teacherId": "<teacher-id>"
}
```

Submit as the administrator acting on the demo student and save `<submission-id>`:

```http
POST /assignment-service/api/v1/assignments/<assignment-id>/submissions
```

```json
{
  "studentId": "<student-id>",
  "content": "Walkthrough submission created through the API."
}
```

Grade and release:

```http
PATCH /assignment-service/api/v1/assignments/submissions/<submission-id>/grade
PATCH /assignment-service/api/v1/assignments/submissions/<submission-id>/release-grade
```

```json
{
  "teacherId": "<teacher-id>",
  "score": 92.5,
  "feedback": "Clear architecture and good tests."
}
```

The release request uses only `teacherId`. Before release, public submission responses hide score and feedback; after release, they expose both.

### Expected responses

| Status | Meaning |
| --- | --- |
| `200 OK` | Read, update, or action succeeded |
| `201 Created` | Resource creation succeeded |
| `400 Bad Request` | Request validation failed |
| `401 Unauthorized` | Token is missing, invalid, or expired |
| `403 Forbidden` | The authenticated role cannot perform the action |
| `404 Not Found` | The requested resource does not exist |
| `409 Conflict` | A uniqueness or domain rule was violated |
| `503 Service Unavailable` | A required service could not be reached |
