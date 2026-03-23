# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

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
