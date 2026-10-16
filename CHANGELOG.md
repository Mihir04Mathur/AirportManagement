# Airport Management System - what changed in each version

## Version 1 - Review 1 (GUI, database, JDBC)
* Swing GUI: home menu with 6 buttons, Flights, Passengers, Employees, Book Ticket, Check-In, View Bookings.
* SQLite database with 6 tables (passenger, employee, flight, booking, payment, checkin), keys and CHECK rules.
* JDBC with PreparedStatement, transactions (booking, cancel, check-in) and 4 DAO classes.
* OOP: Person/Employee/Pilot/CabinCrew/AirportStaff, Flight/Domestic/International, Payment/Card/UPI/Cash,
  interfaces Bookable and Payable, overloading, overriding, ArrayList, custom exceptions.

## Version 2 - Review 2 (packages, interfaces, Java library, exception handling)
* 8 packages: model, interfaces, exception, db, service, util, ui, plus Main.
* 5 interfaces: Bookable (default + static methods), Payable, CrudDAO<T>, Searchable<T>, Exportable.
* Service layer (BookingService, ReportService) with business rules.
* Java library: Streams, Collectors, Comparator, Optional, java.time, java.io / java.nio, java.util.logging, NumberFormat.
* 12 custom exceptions (checked + one unchecked), multi-catch, finally, exception chaining, global exception handler.
* New screens: Reports & Export (statistics, sorting, CSV/TXT export) and Activity Log.
* Rules: no booking / cancel / check-in after departure, passport needed for international flights.

## Version 3 - UI/UX and feature upgrade (this version)
### Security and access
* Login screen. Passwords are stored as salted SHA-256 hashes (java.security), never as plain text.
* Two roles: ADMIN (everything) and STAFF (no Employees, Reports, Activity Log). Locked cards show a padlock.
* 3 wrong passwords lock that account for 30 seconds. Every login, failure and logout is written to the log.
* Sign out button, Ctrl+L shortcut, password is wiped from memory after each attempt.

### New features
* Seat map: click a free seat on a drawn plane layout (business rows 1-3 tinted, taken seats greyed out) or use Auto.
  Auto-assign now respects class: business gets rows 1-3, economy gets row 4 onwards.
* Refund on cancellation: 90% of the fare if cancelled 24 hours or more before departure, 50% after that.
  The refund is shown before you confirm, stored in the database, shown in View Bookings and in Reports.
* Passenger trip history: "Trips" button shows every booking of a passenger with totals.
* Boarding pass / e-ticket is now a drawn ticket with a tear-off stub and barcode, and can be saved as a file.
* Flights table: Pilot column and a coloured Status column (Open / Few seats / Sold out / Departed).
* Home screen: "Next departures" strip, signed-in user, Sign out.
* Reports: new "Refunds issued" figure (9 key numbers).

### UI / UX
* Toast messages replace most pop-up boxes: success messages appear at the bottom and fade away by themselves.
* Placeholder hints inside every form field (for example "10-digit mobile number").
* Date and time pickers (arrow keys change the selected part) instead of typing dates in text boxes.
* Click any table header to sort. Numbers sort as numbers (even with the rupee sign, % or #).
* Empty tables show "No records to show".
* Double-click a booking to open its ticket.
* Keyboard shortcuts: Esc = Home, F5 = Refresh, Ctrl+L = Sign out.
* Status footer with the current user and a live clock; window icon; gradient login page.
* Indian number grouping for money: Rs. 12,34,567.50.

### Database
* New table user_account. New column booking.refund_amount.
* Automatic migration: a database created by an older version is upgraded on start (nothing is lost).
* Sample data now includes bookings, payments and a check-in, and flight dates are relative to today.

### Quality
* 101 automated checks (backend 52 + 28, migration 3, real-window GUI test 18) all passing.
* The GUI test starts the real window, logs in, fills forms, picks a seat, pays, cancels, checks in and logs out.
