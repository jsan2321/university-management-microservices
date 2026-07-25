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

Keycloak owns credentials, sessions, roles, and token issuance. Business services own university-domain profiles and records. A student or teacher profile is linked to a Keycloak subject, and browser clients do not submit profile identifiers to establish their own identity.

## Local infrastructure

The development Compose file starts PostgreSQL and Keycloak. PostgreSQL initialization scripts create the logical databases used by student, academic, enrollment, attendance, assignment, identity, and Keycloak data. Keycloak imports the local `ums` realm from `docker/keycloak/dev-import/ums-realm.json`.

See [local development](local-development.md) for startup and reset instructions, the [domain model](domain-model.md) for ownership and relationships, and [API reference](api.md) for service routing.
