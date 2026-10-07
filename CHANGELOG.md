# Changelog

Notable changes to the Airport Management System are recorded here.

## [2.0.0] - 2026-10-07

V2 is the current stable version and builds on the original V1 application.

### Added

- Admin and staff sign-in with role-based screen access.
- Seat-map selection, passenger trip history, and saved boarding passes.
- Cancellation refunds, reports, CSV/TXT export, and activity logging.
- Date/time pickers, sortable tables, keyboard shortcuts, and transient
  confirmation messages.
- Database initialization and migration from an older database schema.

### Improved

- Expanded service, DAO, interface, exception, and utility layers.
- Updated flight and booking displays with pilot, availability, and departure
  status information.
- Added business/economy-aware automatic seat assignment.

## [1.0.0]

- Initial Swing airport-management application with passenger, employee, and
  flight management, booking, check-in, and SQLite persistence.
- Preserved source snapshot: [`archive/v1`](archive/v1/).
