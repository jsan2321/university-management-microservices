# Architecture

## System shape

The system is a Spring Boot microservices backend with a React browser application. The browser authenticates with Keycloak using Authorization Code with PKCE and sends bearer tokens to the API Gateway. The gateway applies CORS and role rules, then forwards requests to business services through Eureka discovery.

Configuration is served centrally by `config-server`. PostgreSQL runs as one local container with a separate logical database per business service; services do not use database-level foreign keys across those boundaries.

## Service responsibilities

| Area | Services | Responsibility |
| --- | --- | --- |
| Platform | `config-server`, `discovery-server`, `api-gateway` | Configuration, discovery, routing, and edge security |
| Identity | `identity-service`, `security-common` | Keycloak provisioning, profile linking, JWT audience and realm-role handling |
| Academic records | `student-service`, `academic-service`, `enrollment-service` | People, catalog, sections, and enrollment lifecycle |
| Teaching workflows | `attendance-service`, `assignment-service` | Attendance, assignments, submissions, grades, and release workflow |
| Browser application | `frontend` | Role-aware administrator, teacher, and student experience |

## Domain and integration rules

Business services follow a domain, application, and infrastructure separation. Cross-service relationships are stored as IDs and resolved through service APIs. Internal endpoints are reserved for service-to-service calls and are not exposed in public API documentation or through the gateway.

`/internal/academic` is the Academic Service's read-only service contract. Student, enrollment, attendance, and assignment services use it to validate programs, enrollment eligibility, section ownership, and assignments without accessing the Academic Service database. Browser clients use only gateway-routed public APIs; `/teachers/me` and `/students/me` resolve the caller's profile from the JWT subject.

Keycloak owns credentials, sessions, roles, and token issuance. Business services own university-domain profiles and records. A student or teacher profile is linked to a Keycloak subject, and browser clients do not submit profile identifiers to establish their own identity.

Identity provisioning generates immutable student/teacher codes, usernames, and university email addresses in the identity service. Administrators provide a personal contact email for the Keycloak verification/password-setup invitation; they never provide or see an initial password. Academic catalog and enrollment records use deactivation/cancellation rather than hard deletion to retain historical integrity.

## Local infrastructure

The development Compose file starts PostgreSQL and Keycloak. PostgreSQL initialization scripts create the logical databases used by student, academic, enrollment, attendance, assignment, identity, and Keycloak data. Keycloak imports the local `ums` realm from `docker/keycloak/dev-import/ums-realm.json`.

See [local development](local-development.md) for startup and reset instructions, the [domain model](domain-model.md) for ownership and relationships, and [API reference](api.md) for service routing.
