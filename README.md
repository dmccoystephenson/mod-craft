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

```
mvn clean test
```

If you see `BUILD SUCCESS`, the tests have passed.

## Development

### Local Development

1. Clone the repository and open it in your IDE.
2. Run the application using the Spring Boot Maven plugin:
   ```
   mvn spring-boot:run -Dspring-boot.run.profiles=dev
   ```
3. The H2 in-memory database is reset on each restart. To use the H2 console, activate the `dev` profile (see [Configuration Guide](CONFIG.md)).

## Usage reporting

Usage reporting is on by default: when the service starts it sends one `startup` event, carrying its name (`mod-craft`), its version and the tag `service=true`, to the maintainers' [trace](https://github.com/Stephenson-Software/trace) service at `https://trace.danielstephenson.dev`, so that it is known whether anybody runs it. Nothing is sent per request, and nothing about mods, modpacks, users, hosts or IP addresses is ever included. The event goes out on a background thread and is dropped if the trace server is down or slow, so it can never delay start-up or a request. One line is logged at start-up saying whether reporting is on and, if it is off, which switch turned it off.

To turn it off, any one of these is enough:

- `USAGE_REPORTING_ENABLED=false` in the environment (or `--usage-reporting.enabled=false` on the command line, or `usage-reporting.enabled=false` in `application.properties`)
- `TRACE_USAGE_REPORTING=off` (also `false`, `0`, `no`) in the environment — the switch every trace client honours, checked before the service's own setting
- `DO_NOT_TRACK=1` (also `true`, `yes`) in the environment, per [consoledonottrack.com](https://consoledonottrack.com)

The bundled `usage-reporting.key` is the write key issued to mod-craft; it can only add usage events and is not secret. See [`usage-reporting.enabled`](CONFIG.md#usage-reportingenabled) in the Configuration Guide, and for what trace collects and why: https://github.com/Stephenson-Software/trace#usage-reporting

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
