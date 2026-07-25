# Local development

## Development boundary

This repository provides an explicit local-development environment:

- `compose.dev.yml` starts local PostgreSQL and Keycloak containers.
- `docker/keycloak/dev-import/` contains a development realm with known local credentials.
- Config Server contains only `*-dev.yml` service configuration.
- Spring services must run with the `dev` profile.

These assets are not a production deployment template. Production requires a separate deployment definition, managed secrets, production domains and TLS, a production Keycloak provisioning process, and an appropriate Config Server backend.

Spring profiles control application configuration. Compose files control containers, and Keycloak realm imports control identity-provider state. A Spring `prod` profile alone cannot make a development Compose file or realm import production-safe.

## Prerequisites

- Java 21
- Docker Desktop or another Docker engine with Docker Compose
- Node.js 20+ and npm

## Configuration

Copy `.env.example` to `.env` at the repository root. Every checked-in value is a development default. Keep environment-specific values in `.env`; it is ignored by Git.

```sh
cp .env.example .env
```

PowerShell:

```powershell
Copy-Item .env.example .env
```

The local Keycloak identities are:

| Purpose | Realm | Username | Password |
| --- | --- | --- | --- |
| Application administrator | `ums` | `ums.admin` | `Admin123!` |
| Keycloak bootstrap administrator | `master` | value of `KEYCLOAK_ADMIN` | value of `KEYCLOAK_ADMIN_PASSWORD` |

The bootstrap administrator manages Keycloak itself. The application administrator carries the `ADMIN` realm role used by the University Management System APIs.

## Start infrastructure

```sh
docker compose --env-file .env -f compose.dev.yml up -d
```

The realm import and PostgreSQL initialization run only against fresh state. Existing named-volume data is preserved.

## Start the backend

Start Config Server first:

```sh
./mvnw -pl config-server spring-boot:run
```

Start each remaining service in a separate terminal with the development profile:

```sh
./mvnw -pl discovery-server spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw -pl api-gateway spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw -pl student-service spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw -pl academic-service spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw -pl enrollment-service spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw -pl attendance-service spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw -pl assignment-service spring-boot:run -Dspring-boot.run.profiles=dev
./mvnw -pl identity-service spring-boot:run -Dspring-boot.run.profiles=dev
```

On Windows, replace `./mvnw` with `./mvnw.cmd`.

## Start the frontend

```sh
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

## Optional demo data

After the complete backend is healthy, create or reuse a full example workflow:

```sh
node scripts/seed-demo.mjs
```

The script authenticates as the local `ums.admin`, calls only gateway APIs, and creates catalog, identity, enrollment, attendance, assignment, submission, and released-grade data. It is safe to rerun because it uses stable demo codes and provisioning idempotency keys.

Teacher and student passwords are temporary and must be changed on their first interactive login. See the [API walkthrough](api-walkthrough.md) to perform the same flow manually.

## Local URLs

| Component | URL |
| --- | --- |
| Frontend | `http://localhost:5173` |
| API Gateway | `http://localhost:8080` |
| Student service | `http://localhost:8081` |
| Academic service | `http://localhost:8082` |
| Enrollment service | `http://localhost:8083` |
| Attendance service | `http://localhost:8084` |
| Assignment service | `http://localhost:8085` |
| Identity service | `http://localhost:8086` |
| Config Server | `http://localhost:8888` |
| Eureka | `http://localhost:8761` |
| Keycloak | `http://localhost:8180` |

## Preserve or reset local data

Stop containers and preserve the PostgreSQL named volume:

```sh
docker compose --env-file .env -f compose.dev.yml down
```

Permanently remove all local service databases, Keycloak users/state, and Flyway history:

```sh
docker compose --env-file .env -f compose.dev.yml down -v
```

The second command is destructive. On the next `up`, PostgreSQL recreates the logical databases, Keycloak reimports the development realm, and Flyway recreates service schemas. Domain sample data returns only if the optional seeder is run again.

Deleting containers or Docker images does not normally delete the named volume. `down -v` or an explicit volume removal does.

## Verify

```sh
./mvnw test
```

From `frontend/`:

```sh
npm run lint
npm test
npm run build
```

## Troubleshooting

- If Config Server cannot find a service configuration, confirm that the service was started with the `dev` profile.
- If a service cannot obtain configuration, check `http://localhost:8888/actuator/health`.
- If routing fails, ensure Eureka is healthy at `http://localhost:8761/actuator/health` and the target service is registered.
- Realm imports and PostgreSQL init scripts do not replace existing named-volume state. Use the documented destructive reset only when existing local data can be discarded.
