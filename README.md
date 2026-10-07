# Airport Management System

This repository contains two stages of the Airport Management System:

- **V1** - the original version, in [`V1/AirportManagement`](V1/AirportManagement/).
- **V2** - the updated version, in [`V2/V2AirportManagement`](V2/V2AirportManagement/).

Each version is a standalone Java Swing application with its own source code,
SQL scripts, run scripts, and README. See the version-specific README for
features and detailed setup instructions.

## Run a version

Install JDK 11 or newer, then run its startup script from that version's project
directory:

- V1: `V1\AirportManagement\run.bat` on Windows or
  `V1/AirportManagement/run.sh` on Linux/macOS.
- V2: `V2\V2AirportManagement\run.bat` on Windows or
  `V2/V2AirportManagement/run.sh` on Linux/macOS.

The SQLite database, logs, compiled output, and ZIP archives are local/generated
files and are excluded from version control.
