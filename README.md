# mod-craft

## Description

mod-craft is a Spring Boot REST API that lets you manage individual Minecraft mods and assemble them into modpacks. It provides full CRUD operations for mods and modpacks, supports filtering by Minecraft version and name search, and exposes a dedicated build endpoint that returns a fully assembled modpack with all of its mods.

## Installation

### First Time Installation

1. Ensure you have Java 17 and Maven installed.
2. Clone the repository:
   ```
   git clone https://github.com/dmccoystephenson/mod-craft.git
   ```
3. Build the project:
   ```
   mvn clean package
   ```
4. Run the application:
   ```
   java -jar target/mod-craft-*.jar
   ```
5. The API is available at `http://localhost:8080`.

## Usage

### Documentation

- [User Guide](USER_GUIDE.md) – Getting started and common scenarios
- [API Endpoint Reference](COMMANDS.md) – Complete list of all REST endpoints
- [Configuration Guide](CONFIG.md) – Detailed configuration options

## Support

You can find the support Discord server [here](https://discord.gg/xXtuAQ2).

### Experiencing a bug?

Please fill out a bug report [here](https://github.com/dmccoystephenson/mod-craft/issues/new).

- [Known Bugs](https://github.com/dmccoystephenson/mod-craft/issues?q=is%3Aissue+is%3Aopen+label%3Abug)

## Contributing

- [CONTRIBUTING.md](CONTRIBUTING.md)

## Testing

### Unit Tests

Linux:

```
./mvnw clean test
```

Windows:

```
mvnw.cmd clean test
```

If you see `BUILD SUCCESS`, the tests have passed.

## Development

### Local Development

1. Clone the repository and open it in your IDE.
2. Run the application using the Spring Boot Maven plugin:
   ```
   ./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
   ```
3. The H2 in-memory database is reset on each restart. To use the H2 console, activate the `dev` profile (see [Configuration Guide](CONFIG.md)).

## Authors and Acknowledgement

### Developers

| Name | Main Contributions |
|------|-------------------|
| dmccoystephenson | Initial implementation |

## License

This project is licensed under the [GNU General Public License v3.0](LICENSE) (GPL-3.0).

You are free to use, modify, and distribute this software, provided that:

- Source code is made available under the same license when distributed.
- Changes are documented and attributed.
- No additional restrictions are applied.

See the [LICENSE](LICENSE) file for the full text of the GPL-3.0 license.

## Project Status

This project is in active development.

### Changelog

See [CHANGELOG.md](CHANGELOG.md) for a release-by-release summary of changes.
