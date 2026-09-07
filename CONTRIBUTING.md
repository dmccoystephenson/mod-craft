# Contributing

## Thank You

Thank you for your interest in contributing to mod-craft! This guide will help you get started.

## Links

- [Website](https://dansplugins.com)
- [Discord](https://discord.gg/xXtuAQ2)

## Requirements

- A GitHub account
- Git installed on your local machine
- Java 17 or newer
- Maven 3.9+
- A Java IDE or text editor
- A basic understanding of Java and Spring Boot

## Getting Started

1. [Sign up for GitHub](https://github.com/signup) if you don't have an account.
2. Fork the repository by clicking **Fork** at the top right of the repo page.
3. Clone your fork:
   ```
   git clone https://github.com/<your-username>/mod-craft.git
   ```
4. Open the project in your IDE.
5. Build the project:
   ```
   mvn clean package
   ```
   If you encounter errors, please open an issue.

## Identifying What to Work On

### Issues

Work items are tracked as [GitHub issues](https://github.com/dmccoystephenson/mod-craft/issues).

### Milestones

Issues are grouped into [milestones](https://github.com/dmccoystephenson/mod-craft/milestones) representing upcoming releases.

## Making Changes

1. Make sure an issue exists for the work. If not, create one.
2. Switch to `develop`: `git checkout develop`
3. Create a branch: `git checkout -b <branch-name>`
4. Make your changes.
5. Test your changes (see [Testing](#testing)).
6. Commit: `git commit -m "Description of changes"`
7. Push: `git push origin <branch-name>`
8. Open a pull request against `develop`, link the related issue with `#<number>`.
9. Address review feedback.

## Testing

Run the unit tests with:

Linux:
```
./mvnw clean test
```

Windows:
```
mvnw.cmd clean test
```

## Questions

Ask in the [Discord server](https://discord.gg/xXtuAQ2).
