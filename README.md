# University Management System

Spring Boot microservices project for a university management system. The project is built as a portfolio-grade backend using DDD, hexagonal architecture, Spring Cloud, PostgreSQL, Flyway, Keycloak, Docker, and automated tests.

## Stack

- Java 21
- Spring Boot 4.x
- Spring Cloud 2025.x
- Spring Cloud Config
- Eureka Discovery Server
- Spring Cloud Gateway MVC
- OpenFeign
- Spring Security and OAuth 2.0 Resource Server
- Keycloak
- PostgreSQL
- Flyway
- Maven
- Docker Compose
- JUnit and Mockito
- SpringDoc OpenAPI

## Modules

| Module | Purpose | Status |
| --- | --- | --- |
| `config-server` | Centralized configuration | Implemented |
| `discovery-server` | Eureka service registry | Implemented |
| `api-gateway` | API entry point, routing, JWT validation, role enforcement | Implemented |
| `student-service` | Student profiles and statuses | Implemented |
| `academic-service` | Departments, programs, teachers, subjects, semesters, sections | Implemented |
| `enrollment-service` | Student enrollments and enrolled sections | Implemented |
| `attendance-service` | Attendance sessions, records, and percentages | Implemented |
| `assignment-service` | Assignments, submissions, grading, and grade release | Implemented |
| `identity-service` | One-step Keycloak account and domain-profile provisioning | Implemented |
| `security-common` | Shared JWT, realm-role, audience, and internal-client security | Implemented |

Authentication is handled by Keycloak.

## Architecture

Business services follow a DDD and hexagonal architecture style:

```text
service
+-- domain
|   +-- model and domain rules
+-- application
|   +-- use cases, commands, ports, application services
+-- infrastructure
    +-- REST controllers, persistence adapters, Feign clients, config
```

Each service owns its own data. Cross-service relationships are stored as IDs and validated through service APIs instead of database-level foreign keys across service databases.

## Local Infrastructure

Docker Compose starts:

- PostgreSQL on port `5432`
- Keycloak on port `8180`

Local development uses one PostgreSQL container with multiple logical databases:

```text
student_db
academic_db
enrollment_db
attendance_db
assignment_db
identity_db
keycloak_db
```

The databases are created by:

```text
docker/postgres/init/01-create-databases.sql
```

Keycloak imports the local `ums` realm from:

```text
docker/keycloak/import/ums-realm.json
```

## Environment

Create a root `.env` file for local values.

Important defaults:

```env
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
POSTGRES_PORT=5432

DB_HOST=localhost
DB_PORT=5432
DB_USERNAME=postgres
DB_PASSWORD=postgres

KEYCLOAK_PORT=8180
KEYCLOAK_ADMIN=admin
KEYCLOAK_ADMIN_PASSWORD=admin
KEYCLOAK_ISSUER_URI=http://localhost:8180/realms/ums
KEYCLOAK_INTERNAL_CLIENT_SECRET=local-internal-secret-change-me
KEYCLOAK_PROVISIONER_CLIENT_SECRET=local-provisioner-secret-change-me
FRONTEND_ALLOWED_ORIGINS=http://localhost:3000,http://localhost:5173
```

Use `DB_HOST=localhost` when running Spring services from the IDE. If services are later run inside Docker, use `DB_HOST=postgres`.

## Run Locally

Start the complete local system with one command:

```powershell
.\scripts\start-all.ps1
```

The launcher starts PostgreSQL and Keycloak, waits for them, starts Config Server and Eureka in order, and then starts the gateway and business services. Runtime logs are written under `.run/logs`.

Check or stop the complete system with:

```powershell
.\scripts\status-all.ps1
.\scripts\stop-all.ps1
```

`stop-all.ps1` preserves the PostgreSQL Docker volume and its data.

For IntelliJ's built-in API client, copy `http-client.private.env.json.example` to `http-client.private.env.json`, fill in the Keycloak UUIDs, and open [http/ums-api.http](docs/http/ums-api.http). Select the `local` environment and run requests with the green play buttons.

### Manual alternative

Start infrastructure:

```powershell
docker compose up -d
```

Run services from the IDE or with Maven. A typical local startup order is:

1. `config-server`
2. `discovery-server`
3. `api-gateway`
4. business services

Run all Maven tests:

```powershell
.\mvnw.cmd test
```

Run one module:

```powershell
.\mvnw.cmd -pl api-gateway test
```

## Local URLs

| Service | URL |
| --- | --- |
| API Gateway | `http://localhost:8080` |
| Keycloak | `http://localhost:8180` |
| Eureka | `http://localhost:8761` |
| Config Server | `http://localhost:8888` |

Default local Keycloak admin credentials come from `.env`.

## Identity Model

Keycloak owns:

- users
- credentials
- roles
- login
- sessions
- token issuing

Business services own domain profiles and records. Every new student and teacher profile links to exactly one Keycloak user through `userId`; the same Keycloak user cannot be linked to a second profile of the same type.

Students and teachers should not freely self-register as active system users. The recommended flow is:

1. The frontend signs an administrator in through Keycloak.
2. The administrator submits one request to `identity-service`.
3. `identity-service` creates a disabled Keycloak user, assigns the role, creates the domain profile, and then enables the account.
4. The user changes the temporary password at first login.

Use `POST /identity-service/api/v1/provisioning/teachers` or `/students` with an `Idempotency-Key` header. Existing accounts and profiles can be connected through the corresponding `/link` endpoint.

The frontend uses Authorization Code with PKCE through the public `ums-web` client. After login, student and teacher screens should use `/me` endpoints; database profile IDs are derived from the JWT subject and are not trusted from browser request bodies.

Public registration can be added later as an applicant/admission workflow.

## Notes

PostgreSQL init scripts and Keycloak realm import are easiest to apply with a fresh local volume. If the local containers were already started before these files existed, recreate the local volumes only if you do not need the existing local data:

```powershell
docker compose down -v
docker compose up -d
```
