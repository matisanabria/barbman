# Barbman (BETA)

Desktop application for small barbershop management. Built with Java and JavaFX, runs fully offline using an embedded SQLite database.

> Currently in beta. A major refactor with UI redesign and multi-language support is in progress.

## Requirements

- Java 17+
- Maven 3.x (or use the included `mvnw` wrapper)

## Setup

```bash
./mvnw javafx:run
```

On Windows:

```bash
mvnw.cmd javafx:run
```

To build a fat JAR:

```bash
./mvnw package
java -jar target/core-1.2.1.jar
```

**Note:** On first run, the database must be seeded manually using [DB Browser for SQLite](https://sqlitebrowser.org/). A setup wizard is planned for a future version. Run the app as administrator to ensure proper file access.

## Features

- User authentication and automatic salary calculation
- Daily, weekly, and monthly production tracking
- Expense management
- Cash and payment reports
- Advance payment support
- Embedded SQLite database in `data/` — no installation required
- Logs saved automatically to `logs/`

## Stack

- Java 17
- JavaFX 17
- SQLite (via xerial JDBC)
- Log4j 2
- Maven

## License

[Apache License 2.0](./LICENSE)
