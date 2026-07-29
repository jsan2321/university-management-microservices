# Environments and Operations

This document covers operational guidelines for running the University Management System locally for development, and considerations for deploying to production.

## Local Development Boundary

This repository provides an explicit local-development environment:

- `compose.dev.yml` starts local PostgreSQL, Redis, Mailhog, Kafka, and Keycloak containers.
- `docker/keycloak/dev-import/` contains a development realm with known local credentials.
- Config Server contains only `*-dev.yml` service configuration.
- Spring services must run with the `dev` profile.

These assets are not a production deployment template. Production requires a separate deployment definition, managed secrets, production domains and TLS, a production Keycloak provisioning process, and an appropriate Config Server backend.

For step-by-step startup instructions for the development environment, refer to the **[root README](../README.md)**.

## Local Test Credentials

When running the local `ums` realm import, the following identities are available:

| Purpose | Realm | Username | Password |
| --- | --- | --- | --- |
| Application administrator | `ums` | `ums.admin` | `Admin123!` |
| Keycloak bootstrap administrator | `master` | value of `KEYCLOAK_ADMIN` | value of `KEYCLOAK_ADMIN_PASSWORD` |

The bootstrap administrator manages Keycloak itself. The application administrator carries the `ADMIN` realm role used by the University Management System APIs.

## Local URLs

| Component | URL | Note |
| --- | --- | --- |
| Frontend | `http://localhost:5173` | React web portal |
| API Gateway | `http://localhost:8080` | Entry point for browser clients |
| Student service | `http://localhost:8081` | Direct service port (Swagger) |
| Academic service | `http://localhost:8082` | Direct service port (Swagger) |
| Enrollment service | `http://localhost:8083` | Direct service port (Swagger) |
| Attendance service | `http://localhost:8084` | Direct service port (Swagger) |
| Assignment service | `http://localhost:8085` | Direct service port (Swagger) |
| Identity service | `http://localhost:8086` | Direct service port (Swagger) |
| Config Server | `http://localhost:8888` | Centralized configuration |
| Eureka | `http://localhost:8761` | Service discovery registry |
| Keycloak | `http://localhost:8180` | Authentication server |
| Mailhog | `http://localhost:8025` | Web UI to view captured local emails |
| Redis | `localhost:6379` | API Gateway rate-limiting cache |

## Preserve or reset local data

Stop containers and preserve the PostgreSQL named volume:

```sh
docker compose --env-file .env -f compose.dev.yml down
```

Permanently remove all local service databases, Keycloak users/state, Redis cache, and Flyway history:

```sh
docker compose --env-file .env -f compose.dev.yml down -v
```

The second command is destructive. On the next `up`, PostgreSQL recreates the logical databases, Keycloak reimports the development realm, and Flyway recreates service schemas. Domain sample data returns only if the optional seeder is run again.

Deleting containers or Docker images does not normally delete the named volume. `down -v` or an explicit volume removal does.

## Troubleshooting

- If Config Server cannot find a service configuration, confirm that the service was started with the `dev` profile.
- If a service cannot obtain configuration, check `http://localhost:8888/actuator/health`.
- If routing fails, ensure Eureka is healthy at `http://localhost:8761/actuator/health` and the target service is registered.
- Realm imports and PostgreSQL init scripts do not replace existing named-volume state. Use the documented destructive reset only when existing local data can be discarded.

---

## Production Deployment

The `compose.prod.yml` file defines the architecture for a production-like environment:

- **Separation of Concerns:** It relies on pre-built Docker images rather than building from source locally.
- **Resource Constraints & Restarts:** Includes `restart: unless-stopped` rules and stricter health checks.
- **External Dependencies:** Expects external DNS, TLS termination (usually via a reverse proxy or load balancer), and a secure Identity Provider (Keycloak) setup.

### Configuration

To deploy using `compose.prod.yml`, you must provide the necessary production secrets and environment variables.

1. **Copy the Production Template:**
   ```sh
   cp .env.prod.example .env.prod
   ```

2. **Configure Secrets:**
   Open `.env.prod` and replace all placeholders (like `CHANGE_ME_strong_password`) with secure, randomly generated secrets. 
   - `POSTGRES_PASSWORD`: The root database password.
   - `KEYCLOAK_ADMIN_PASSWORD`: The master realm Keycloak administrator password.
   - `KEYCLOAK_INTERNAL_CLIENT_SECRET` & `KEYCLOAK_PROVISIONER_CLIENT_SECRET`: Secure client credentials for service-to-service communication.

3. **Configure Domains and URIs:**
   Update the domain properties to match your production environment:
   - `KC_HOSTNAME`: Your Keycloak authentication domain (e.g., `auth.yourdomain.com`).
   - `KEYCLOAK_ISSUER_URI`: The full URL to the `ums` realm.
   - `FRONTEND_ALLOWED_ORIGINS`: The domain where your React frontend is hosted.

### Deployment Steps

1. **Build and Publish Images:**
   Ensure that the Docker images for your services have been built and pushed to your container registry. The `compose.prod.yml` file uses the `${REGISTRY}` and `${TAG}` variables to pull these images.

2. **Start the Infrastructure:**
   ```sh
   docker compose --env-file .env.prod -f compose.prod.yml up -d
   ```

3. **Keycloak Realm Configuration:**
   Unlike the development setup, `compose.prod.yml` does **not** auto-import the `ums-realm.json` development file. You must manually create the `ums` realm in your production Keycloak instance, configure the appropriate clients (`ums-web`, `ums-internal`, `ums-provisioner`), and set up the required realm roles (`ADMIN`, `TEACHER`, `STUDENT`).

### Security Considerations

- **TLS / HTTPS:** The `compose.prod.yml` exposes services on standard HTTP ports (e.g., `8080`, `8180`). In a production environment, these ports should not be exposed directly to the internet. Instead, they should sit behind a reverse proxy (such as Nginx, Traefik, or an AWS Application Load Balancer) that handles TLS termination.
- **Database Access:** The PostgreSQL database port (`5432`) is exposed to the host by default in the compose file for convenience. In a strict production environment, you should remove this port mapping so the database is only accessible within the internal Docker network.
