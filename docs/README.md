# Technical documentation

This directory contains the public technical reference for the University Management System. It intentionally excludes local plans, test identities, request environments, and other machine-specific material.

- [Architecture](architecture.md) explains service boundaries, request flow, security, and local infrastructure.
- [Domain model](domain-model.md) documents service data ownership, relationships, and implemented lifecycle rules.
- [Local development](local-development.md) covers prerequisites, configuration, startup, URLs, verification, and troubleshooting.
- [Production deployment](production-deployment.md) guides deploying the system using the production compose file, secrets, and environment configurations.
- [Audit and messaging](audit-and-messaging.md) explains the event-driven architecture, the Transactional Outbox pattern, and how to run the audit service locally.
- [API reference](api.md) documents gateway route prefixes, access roles, endpoint domains, and local Swagger UI.
- [API walkthrough](api-walkthrough.md) builds a complete example dataset through the secured gateway.

For frontend-specific development, see the [frontend README](../frontend/README.md).
