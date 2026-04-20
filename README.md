# MustelaLogAPI

MustelaLogAPI is a PHP-based HTTP/JSON logging API designed for MySQL-backed deployments and shared hosting environments such as IONOS.

It acts as a secure middleware layer between clients and the logging database. Instead of exposing the database directly to the internet, MustelaLogAPI provides controlled endpoints for log ingestion, querying, source management, and audit-aware administrative access.

## Positioning within the Mustela ecosystem

This repository is intentionally separate from **Mustela**.

- **Mustela** is the broader logging framework and reusable logging foundation.
- **MustelaLogAPI** is a deployable service layer built around that ecosystem.
- Its purpose is to provide a practical, hosting-compatible web API for environments where direct database access is not appropriate.

This separation keeps framework concerns and service concerns cleanly divided. Reusable components may later be merged or extracted into Mustela, while hosting-specific and API-specific logic can remain here.

## Why this repository exists

The project was created to support a real-world deployment scenario:

- PHP middleware
- Three-tier architecture
- MySQL database
- IONOS shared hosting
- No dependency on `.htaccess`
- No root access
- No Docker or long-running background services
- Strong focus on security, maintainability, and future extensibility

## Core goals

- Provide a secure logging middleware for internet-facing access
- Prevent direct client access to the logging database
- Support structured log ingestion over HTTP/JSON
- Offer controlled read access to logs through stable API contracts
- Keep the middleware decoupled from physical database table design
- Support a professional architecture that can evolve without constant redesign
- Remain understandable, well documented, and suitable for long-term maintenance

## Planned responsibilities

MustelaLogAPI is expected to cover areas such as:

- authentication and authorization
- input validation and normalization
- secure database access
- log ingestion
- log querying
- source registration and source control
- audit logging of sensitive operations
- stable API versioning
- structured error handling
- integration with database views and controlled write paths

## Non-goals

At this stage, this repository is **not** meant to replace the Mustela core framework.

It is also not intended to become an unstructured collection of endpoint scripts. The goal is a clean application architecture with clear layers such as bootstrap, routing, controllers, services, repositories, validation, security, and infrastructure support.

## Architecture direction

The intended architecture is:

- **Client layer**
- **PHP middleware (MustelaLogAPI)**
- **MySQL logging database**

The middleware should expose a stable HTTP/JSON API while internally protecting and abstracting the underlying schema. Read operations may rely on stable database views, while write operations should follow controlled service paths rather than unrestricted direct table access.

## Hosting assumptions

MustelaLogAPI is being designed with shared hosting constraints in mind.

Important assumptions include:

- primary `index.php` in the project root
- optional secondary `public/index.php` only as an additional compatibility layer
- no required `.htaccess`
- no required Apache or Nginx reconfiguration
- deployment as a normal PHP project upload, including ZIP-based deployment if needed

## Documentation and code quality

This project is intended to be documented with **phpDocumentor**.

That means:

- classes, methods, properties, and interfaces should have proper DocBlocks
- important business logic should contain explanatory developer comments
- comments should help human understanding, not merely restate obvious syntax
- the codebase should remain readable for maintenance and future extension

## Project status

This repository should currently be regarded as an important early service codebase and architectural foundation.

It may later contribute reusable parts back into the broader Mustela ecosystem, but for now it remains intentionally separate in order to avoid mixing framework code with hosting-specific service implementation too early.

## Roadmap ideas

- define MySQL schema for logging and source management
- define secure API contract
- implement root-based entrypoint and routing
- add authentication and authorization
- add structured ingestion endpoint
- add querying endpoints
- add audit logging
- add phpDocumentor-ready code documentation
- prepare deployment-friendly packaging for shared hosting

## License

License to be defined by the repository owner.
