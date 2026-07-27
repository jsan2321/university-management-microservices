# Audit and Messaging

## Overview

The University Management System uses an event-driven architecture powered by Kafka for asynchronous audit logging. This decouples the core business logic from the audit storage, ensuring that high-throughput operations (like recording attendance or submitting assignments) aren't slowed down by synchronous audit writes.

## Components

### `audit-common`
This is a shared library module (JAR) rather than a standalone service. It provides:
- The domain models and event schemas for audit records.
- Annotations and AOP aspects for declarative audit logging.
- An implementation of the **Transactional Outbox** pattern. This ensures that business data changes and the corresponding audit events are committed in the same database transaction. A background job then reliably relays these events from the outbox table to Kafka.

### `audit-service`
This is an optional, standalone microservice.
- It acts as a Kafka consumer, listening to the configured audit topics.
- It consumes the events and persists them securely into its own logical PostgreSQL database schema for long-term storage and querying.

## Running with Messaging

In the local development environment, the audit and messaging infrastructure is disabled by default to save resources. 

To enable it:

1. **Start the Kafka Infrastructure:**
   Use the `messaging` profile when starting your Docker containers to include the Kafka broker.
   ```sh
   docker compose --env-file .env -f compose.dev.yml --profile messaging up -d
   ```

2. **Start the `audit-service`:**
   Start the service normally with the `dev` profile.
   ```sh
   ./mvnw -pl audit-service spring-boot:run -Dspring-boot.run.profiles=dev
   ```

3. **Enable the Outbox in Business Services:**
   When running business services (like `student-service` or `enrollment-service`), you must enable the audit outbox relay and point it to the local Kafka broker.
   You can do this by setting environment variables before running the service:
   
   **Bash:**
   ```sh
   export AUDIT_OUTBOX_ENABLED=true
   export SPRING_KAFKA_BOOTSTRAP_SERVERS=localhost:9092
   ./mvnw -pl student-service spring-boot:run -Dspring-boot.run.profiles=dev
   ```

   **PowerShell:**
   ```powershell
   $env:AUDIT_OUTBOX_ENABLED="true"
   $env:SPRING_KAFKA_BOOTSTRAP_SERVERS="localhost:9092"
   .\mvnw.cmd -pl student-service spring-boot:run -Dspring-boot.run.profiles=dev
   ```
