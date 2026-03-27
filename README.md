# Barbman Desktop (Archived)

Desktop application for small barbershop management. Built with Java 17 and JavaFX, runs fully offline on Windows using an embedded SQLite database.

> **This project has been archived.** Barbman is being rebuilt as a web application. This repository represents the final state of the desktop version.

## Features

- **User authentication** with PIN-based login and role management (admin / user)
- **Point of sale** for services and products with multiple payment methods (cash, transfer, card, QR)
- **Cashbox management** with opening/closing workflow, expected vs actual reconciliation, and discrepancy tracking
- **Financial dashboard** with daily, weekly, and monthly reports including income/expense breakdown, balance tracking, and charts
- **Salary system** supporting commission-based, fixed, and mixed payment types with configurable pay frequency
- **Advance payments** tracked against future salary periods
- **Expense tracking** categorized and linked to cashbox movements
- **Production tracking** per employee across configurable time periods
- **Client management** with appointment history
- **Appointments module** with calendar view
- Fully offline — no internet required

## Stack

- Java 17 + JavaFX 17
- Hibernate ORM 6.6 + SQLite (via hibernate-community-dialects)
- Flyway 9.22 for database migrations
- Lombok for boilerplate reduction
- Log4j 2 for logging
- Jackson for JSON processing
- ControlsFX + Ikonli for UI components and icons
- JNA for Windows-native path resolution
- Maven for build management

## Architecture

```
model/          Domain entities mapped with JPA/Hibernate
repositories/   Interface + Hibernate implementation pairs (GenericRepository<T, ID>)
service/        Business logic (CashboxService, SalesService, SalariesService, etc.)
controller/     JavaFX controllers bound to FXML views
dto/            Read-only projections for views
util/           Cross-cutting utilities (WindowManager, SessionManager, AlertUtil)
infrastructure/ HibernateUtil, FlywayMigrator
```

Data is stored in `Documents/Barbman Data/data/database.db` on the user's Windows profile.

> **Note:** The entire application UI and codebase (comments, variable names, labels) is in Spanish.

## Prerequisites

- **Java 17+**
- **Maven 3.x**
- **JavaFX SDK 17.0.16** — [Download](https://gluonhq.com/products/javafx/) (needed to run the fat JAR outside of an IDE)
- **JavaFX jmods 17.0.16** — same download page (needed only if building a custom runtime image)

## Build & Run

### From IntelliJ

The project runs directly with `mvn javafx:run` using IntelliJ's bundled Maven.

### From command line

```bash
# Run with Maven plugin
mvn javafx:run

# Build fat JAR
mvn package

# Run the fat JAR (requires JavaFX SDK on the module path)
java --module-path /path/to/javafx-sdk-17.0.16/lib --add-modules javafx.controls,javafx.fxml -jar target/core-1.3.1.jar

# Clean build
mvn clean package
```

## Default User

On first run, Flyway seeds a default admin user:

| Name  | Role  | PIN    |
|-------|-------|--------|
| Admin | admin | `0000` |

## License

[Apache License 2.0](./LICENSE)

## A Note from the Developer

Hi, I'm Mati. I originally built this project to learn Java and programming in general. Then I got a client
who was interested in the app and paid me to add features. A lot of the code is tailored to that specific client,
but here's the source code in case you want to use it or adapt it for your own business.
There's probably a lot of room for improvement, so do whatever you want with it. I won't be maintaining or adding
features anymore — it's all yours :)
Don't worry, the client will be getting the web version, so this one is free of vulnerabilities.

---

*Barbman Desktop - 2025/2026. Moving to the web.*
