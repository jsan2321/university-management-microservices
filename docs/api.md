# API reference

## Access model

Use the API Gateway as the supported browser and API-client entry point:

```text
http://localhost:8080/{service-name}/api/...
```

Requests use a Keycloak bearer token. The gateway enforces realm roles before forwarding requests. Internal service-to-service routes under `/internal/**` are never public API endpoints.

For a dependency-ordered set of example requests and payloads, follow the [API walkthrough](api-walkthrough.md). For an automated development dataset, run `node scripts/seed-demo.mjs` from the repository root.

## Gateway routes

| Service | Gateway prefix | API domain | Typical access |
| --- | --- | --- | --- |
| Student | `/student-service` | `/api/v1/students` | Administrator; `/me` is student-only |
| Academic | `/academic-service` | `/api/v1/academic` | Catalog reads for authenticated roles; changes for administrators |
| Enrollment | `/enrollment-service` | `/api/v1/enrollments` | Reads for authenticated roles; changes for administrators |
| Attendance | `/attendance-service` | `/api/v1/attendance` | Reads for authenticated roles; recording for administrators and teachers |
| Assignment | `/assignment-service` | `/api/v1/assignments` | Reads for authenticated roles; authoring for administrators and teachers; submissions for students |
| Identity | `/identity-service` | `/api/v1/provisioning` | Administrator-only |

For example, the student profile endpoint is available through `http://localhost:8080/student-service/api/v1/students/me`.

## Endpoint domains

- **Students:** profile retrieval, status lifecycle, and signed-in student profile resolution.
- **Academic catalog:** departments, programs, teachers, semesters, subjects, and sections.
- **Enrollments:** create, list, retrieve, cancel, and signed-in student enrollment views.
- **Attendance:** sessions, attendance records, section rosters, and percentage views.
- **Assignments:** assignment lifecycle, submissions, grading, and grade release.
- **Identity provisioning:** create and link Keycloak teacher and student accounts with domain profiles.

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

The documentation pages and OpenAPI JSON are publicly readable in local development. Select **Authorize** in Swagger UI and enter a valid bearer token before calling protected business endpoints.
