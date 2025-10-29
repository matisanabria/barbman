# Barbman (BETA)

Barbman* is a desktop application designed for **small business management**.  
It is built in **Java** with **JavaFX** and uses an embedded database (`SQLite`), allowing it to run **offline** on any computer.

---

## Project Status
Currently in **BETA phase**.  
You can download the latest version from the [Releases](../../releases) section.

**Important Notes:**  
- Default **database data** must be loaded manually using a tool like [DB Browser for SQLite](https://sqlitebrowser.org/) or similar.  
  A **setup mode** will be included in future versions to simplify this step.  
- It is recommended to **run the application as administrator** to ensure proper access to files and database.  
- A **major update** is in progress: full code refactoring, migration to English on code, multiple language support, and complete UI redesign.

---

## Main Features

- **User management**: quick authentication and automatic salary calculations.  
- **Record of completed services**: daily, weekly, and monthly production tracking.  
- **Expense management**: control of business expenses.  
- **Reports and statistics**: generation of cash and payment reports.  
- **Advance payments support** in the salary module.  
- **Embedded database** (`SQLite`) → folder `data/`, no installation required.  
- **Logging system**: all logs are automatically saved in the `logs/` folder.

---

## Technologies Used

- **Java 17+**
- **JavaFX 17**
- **SQLite**
- **Log4j 2**
- **Maven**

---

## License

This project is licensed under the [Apache License 2.0](./LICENSE).  
You can use, modify, and distribute it freely, but please give credit to the creator.

---
💼 *Barbman: Manage your small business quickly and easily.*
