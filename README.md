# Airport Management System

A desktop airport-management application built with Java Swing, JDBC, and SQLite.
The current stable release is **V2**. The original V1 source is preserved in
[`archive/v1`](archive/v1/), and the V1 release is tagged `v1.0.0`.

## Features

V2 includes role-based sign-in (admin and staff), management screens for
passengers, employees, flights and bookings, booking and check-in workflows,
reports and CSV/TXT export, activity logging, a seat map, cancellation refunds,
passenger trip history, and a saved boarding pass. Database initialization and
older-database migration are handled by the application.

See [CHANGELOG.md](CHANGELOG.md) for the release summary.

## Requirements

- JDK 11 or newer (`java` and `javac` on `PATH`)
- The SQLite JDBC driver is included in `lib/sqlite-jdbc.jar`

## Run

Run from the repository root so the application can find the SQL scripts:

- **Windows:** `run.bat`
- **Linux/macOS:** `./run.sh`

The scripts compile Java sources from `src/main/java` into `build/classes` and
launch `airport.Main`. Compilation output and runtime data are local and ignored
by Git.

### Demo accounts

The application seeds demo accounts on first startup:

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| Staff | `staff` | `staff123` |

These credentials are for local demonstration only. Do not use them for a
deployed or shared installation.

## Project layout

```text
.
├── archive/v1/                 # Preserved V1 source and run scripts
├── src/main/java/airport/
│   ├── Main.java
│   ├── db/                     # SQLite connection and DAO classes
│   ├── exception/              # Application exceptions
│   ├── interfaces/              # Domain and DAO interfaces
│   ├── model/                   # Airport domain models
│   ├── service/                 # Booking, authentication, and reports
│   ├── ui/                      # Swing screens and dialogs
│   └── util/                    # Logging, export, formatting, validation
├── sql/
│   ├── schema.sql
│   └── sample_data.sql
├── lib/sqlite-jdbc.jar
├── run.bat
└── run.sh
```

The existing `airport` package hierarchy is retained rather than renaming all
classes as part of a repository reorganization. Source files follow the Java
package-to-directory convention and use the conventional `src/main/java` source
root.

## Build verification

GitHub Actions compiles the project with JDK 11 on pushes and pull requests.
There is no automated test suite included in this repository yet.

## Releases and versioning

`main` contains the current stable V2. Releases are marked with Git tags rather
than long-lived version branches: `v1.0.0` identifies the preserved V1 snapshot,
and `v2.0.0` identifies V2. See the GitHub Releases page for release notes.

Versions follow `MAJOR.MINOR.PATCH`: increase MAJOR for incompatible changes,
MINOR for backward-compatible features, and PATCH for backward-compatible bug
fixes. A substantial feature is not automatically a major version; if existing
users and data remain compatible, it can still be a MINOR release.

Examples for this application:

- `v2.0.0`: the V2 release, a major upgrade from V1 with authentication,
  role-based access, refunds, and database migration.
- `v2.1.0`: a future backward-compatible feature, such as an additional report
  that leaves existing screens and stored data usable.
- `v2.1.1`: a backward-compatible bug fix, such as correcting a report total
  without changing its interface or database format.
- `v3.0.0`: an incompatible change, such as removing a supported data format or
  changing stored booking data in a way that existing databases cannot migrate
  automatically.

Keep `main` on the latest stable version and use short-lived branches for
individual changes. Permanent `V1` and `V2` branches would keep two active lines
of development that must be updated and reconciled separately. The V1 tag
provides a stable pointer to the old snapshot; its Git history preserves how
`main` progressed to V2. A GitHub Release can be published for each tag to add
human-readable notes and downloadable assets, but it does not replace the tag.

## License

This project is licensed under the [MIT License](LICENSE).
