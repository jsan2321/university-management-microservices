# Production Deployment

## Overview

While `compose.dev.yml` provides a fast, integrated environment for local development, it is not suitable for production. For a production deployment, the system is designed to be highly available, secure, and configurable via environment variables, utilizing `compose.prod.yml`.

## The Production Compose File

The `compose.prod.yml` file defines the architecture for a production-like environment:
- **Separation of Concerns:** It relies on pre-built Docker images rather than building from source locally.
- **Resource Constraints & Restarts:** Includes `restart: unless-stopped` rules and stricter health checks.
- **External Dependencies:** Expects external DNS, TLS termination (usually via a reverse proxy or load balancer), and a secure Identity Provider (Keycloak) setup.

## Configuration

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

## Deployment Steps

1. **Build and Publish Images:**
   Ensure that the Docker images for your services have been built and pushed to your container registry. The `compose.prod.yml` file uses the `${REGISTRY}` and `${TAG}` variables to pull these images.

2. **Start the Infrastructure:**
   ```sh
   docker compose --env-file .env.prod -f compose.prod.yml up -d
   ```

3. **Keycloak Realm Configuration:**
   Unlike the development setup, `compose.prod.yml` does **not** auto-import the `ums-realm.json` development file. You must manually create the `ums` realm in your production Keycloak instance, configure the appropriate clients (`ums-web`, `ums-internal`, `ums-provisioner`), and set up the required realm roles (`ADMIN`, `TEACHER`, `STUDENT`).

## Security Considerations

- **TLS / HTTPS:** The `compose.prod.yml` exposes services on standard HTTP ports (e.g., `8080`, `8180`). In a production environment, these ports should not be exposed directly to the internet. Instead, they should sit behind a reverse proxy (such as Nginx, Traefik, or an AWS Application Load Balancer) that handles TLS termination.
- **Database Access:** The PostgreSQL database port (`5432`) is exposed to the host by default in the compose file for convenience. In a strict production environment, you should remove this port mapping so the database is only accessible within the internal Docker network.
