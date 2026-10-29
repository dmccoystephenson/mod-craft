# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Added

- Usage reporting: one `startup` event (name, version, `service=true`) is sent to the trace service on start-up; off with `USAGE_REPORTING_ENABLED=false`, `TRACE_USAGE_REPORTING=off` or `DO_NOT_TRACK=1`
- Usage reporting: the startup event also carries a random installation ID as the tag `install`, so deployments can be counted rather than events. It comes from `TRACE_INSTALL_ID` when set, otherwise from a UUID kept in `<user data dir>/mod-craft/trace-install-id`; no ID is made, read or written while reporting is off

### Fixed

- The usage-reporting "Details" link (startup notice, docs and config comments) now points at https://danielstephenson.dev/usage-reporting, a public page, instead of a link into a private repository that answered 404. The vendored `TraceClient` is trace-client-java 0.6.1, whose server-wide switch file comment carries the same link.
- `COMMANDS.md` now lists `author` as optional on `POST`/`PUT /api/mods` (it was never validated), documents the `400` validation error on mod and modpack create/update, and shows the JSON shape of `400`/`404`/`409` error responses.
- `COMMANDS.md` now states that the `minecraftVersion` and `search` filters on `GET /api/mods` and `GET /api/modpacks` do not combine: when both are given, `search` is ignored.
- `COMMANDS.md` now documents the Spring Boot default error body returned for a malformed JSON body or a non-numeric path ID, the `500` returned by `DELETE /api/mods/{id}` while the mod is still in a modpack, and that deleting a modpack leaves its mods in place.
- The usage-reporting start-up log line, the `UsageReporter` Javadoc and the usage-reporting comment in `application.properties` now mention the random `install` ID (and `TRACE_INSTALL_ID`) that every event carries; they previously listed only the name, version and `service=true`.
- `CONFIG.md` now states that when no version can be determined (no Maven-filtered property and no jar manifest version), the startup event is sent with `version` set to `unknown`; it previously claimed the `version` tag was left out. `COMMANDS.md` now lists the `id` path parameter on `GET` and `DELETE /api/modpacks/{id}`, like their mod counterparts.

## [0.0.1] – 2026-03-22

### Added

- `Mod` JPA entity (name, version, author, description, fileUrl, minecraftVersion).
- `Modpack` JPA entity with `@ManyToMany` relationship to `Mod`.
- Spring Data JPA repositories for `Mod` and `Modpack`.
- `ModService` with CRUD operations, filter by Minecraft version, and name search.
- `ModpackBuilderService` with CRUD operations, add/remove mod, and build (assemble) modpack.
- REST controllers for `/api/mods` and `/api/modpacks`.
- `ModpackRequest` DTO for create/update modpack endpoints to prevent mods injection.
- `GlobalExceptionHandler` returning structured JSON for 404, 409, and 400 responses.
- H2 in-memory database with profile-based configuration (`dev` and test profiles).
- 40 unit and integration tests covering CRUD, filtering, builder operations, and error paths.
- DPC conventions documentation: README, CONTRIBUTING, USER_GUIDE, COMMANDS, CONFIG, LICENSE, CI/release workflows, Copilot instructions.
