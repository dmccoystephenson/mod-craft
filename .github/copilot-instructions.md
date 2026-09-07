# Copilot Instructions

This repository follows the DPC (Dans Plugins Community) conventions defined at
https://github.com/Dans-Plugins/dpc-conventions. Read those conventions before
making any changes.

## Technology Stack

- Language: Java 17
- Build tool: Maven (POM: `pom.xml`)
- Framework: Spring Boot 3.4.3
- Persistence: Spring Data JPA with H2 in-memory database
- Test framework: JUnit 5 (via Spring Boot Test)

## Project Structure

- `src/main/java/com/modcraft/` – Application source code
  - `controller/` – REST controllers (`ModController`, `ModpackController`)
  - `dto/` – Request DTOs (e.g. `ModpackRequest`)
  - `exception/` – Custom exceptions and `GlobalExceptionHandler`
  - `model/` – JPA entities (`Mod`, `Modpack`)
  - `repository/` – Spring Data JPA repositories
  - `service/` – Business logic (`ModService`, `ModpackBuilderService`)
- `src/main/resources/` – `application.properties` and profile overrides
- `src/test/java/` – Unit and integration tests mirroring the main source tree
- `src/test/resources/` – Test-specific properties (`application.properties`)

## Coding Conventions

- All create operations (`createMod`, `createModpack`) must call `entity.setId(null)` before persisting to prevent accidental overwrites.
- Use `ModpackRequest` DTO (not the `Modpack` entity) as the request body for POST and PUT modpack endpoints.
- Follow the existing package structure when adding new classes.
- Keep service methods `@Transactional` and use `readOnly = true` for query methods.

## Contribution Workflow

- Branch from `develop` for all changes.
- Open a pull request against `develop`, not `main`.
- Reference the related GitHub issue in every pull request description.
