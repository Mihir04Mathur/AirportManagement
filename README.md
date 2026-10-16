# Airport Management System  (Java Swing + JDBC + SQLite)

Review 1: GUI design, database design, JDBC.
Review 2: packages, interfaces, Java library, exception handling (see the Review 2 section below).
Version 3: login, seat map, refunds, new UI/UX (see CHANGELOG.md for the full list).

## How to run
Needs JDK 11 or newer (`java -version`, `javac -version`).

* Windows: double-click `run.bat`
* Linux / macOS: `./run.sh`
* Any IDE (VS Code / IntelliJ / Eclipse): add `lib/sqlite-jdbc.jar` to the classpath, mark `src` as the source folder, run `airport.Main`. **Run from the project root folder** (the app reads `sql/schema.sql`).

On first run the app creates `airport.db` with all tables and sample data. Delete `airport.db` to reset.
An `airport.db` from an older version is upgraded automatically.

**Sign in with:** `admin / admin123` (all screens) or `staff / staff123` (no Employees, Reports, Activity Log).
**Shortcuts:** Esc = Home, F5 = Refresh, Ctrl+L = Sign out.

## Folder layout
```
AirportManagement/
 |- lib/sqlite-jdbc.jar          JDBC driver
 |- sql/schema.sql               DATABASE DESIGN (6 tables, keys, checks)
 |- sql/sample_data.sql          demo data (flight dates are relative to today)
 |- logs/airport.log             created at run time by java.util.logging
 |- exports/                     default folder for CSV / TXT exports
 |- src/airport/Main.java        entry point
 |- src/airport/ui/              GUI screens (Swing)
 |- src/airport/service/         business rules + reports (BookingService, ReportService)
 |- src/airport/db/              JDBC (connection + DAO classes)
 |- src/airport/model/           entity classes (OOP hierarchy)
 |- src/airport/interfaces/      Bookable, Payable, CrudDAO<T>, Searchable<T>, Exportable
 |- src/airport/exception/       custom exception hierarchy
 |- src/airport/util/            AppLogger, FileLogHandler, CsvExporter, FormatUtil, Validate
```

## Database design (SQLite)
passenger, employee (Pilot / CabinCrew / AirportStaff in one table with a `role` column),
flight (Domestic / International with a `flight_type` column), booking, payment, checkin.

Relationships: booking -> passenger, booking -> flight, payment -> booking (1:1),
checkin -> booking (1:1), flight -> employee (pilot).
Constraints: PRIMARY KEY AUTOINCREMENT, FOREIGN KEY (enabled with PRAGMA), UNIQUE (flight_no, passport),
CHECK (age, seats, status, role, payment method), partial UNIQUE index so a seat cannot be double-booked.

## JDBC concepts used
DriverManager / Connection, PreparedStatement (no SQL injection), ResultSet mapping,
generated keys, transactions (`setAutoCommit(false)`, commit, rollback) for booking, cancel and check-in,
try-with-resources, joins, LIKE searches.

## OOP concepts and where to find them
| Concept | Where |
|---|---|
| Encapsulation | private fields + getters/setters in every model class |
| Inheritance | `Person -> Passenger`, `Person -> Employee -> Pilot / CabinCrew / AirportStaff`, `Flight -> DomesticFlight / InternationalFlight`, `Payment -> CardPayment / UPIPayment / CashPayment` |
| Polymorphism | `Flight.calculateFare()`, `Payment.validate()/getProcessingFee()`, `Employee.getDetails()` |
| Abstraction | abstract `Person`, `Employee`, `Flight`, `Payment` |
| Interfaces | `Bookable` (Flight), `Payable` (Payment) |
| Constructors | every model class (`new Passenger(...)`, `new DomesticFlight(...)`) |
| Overriding | `taxAndFees()` (5% GST vs 12% tax + Rs.1500), `getProcessingFee()` (card 2%), `getRole()` |
| Overloading | `FlightDAO.search(...)` x3, `BookingDAO.bookTicket(...)` x2, `BookingDAO.checkIn(...)` x2 |
| Collections | `ArrayList<Passenger/Flight/Employee/Booking>`, `HashSet` for taken seats, `HashMap` of screens |
| Exception handling | `AirportException`, `SeatUnavailableException`, `InvalidBookingException`, `PaymentException`, input `Validate` |
| File / DB handling | all data is saved in `airport.db` through JDBC; SQL scripts are read from `sql/` |

## Business rules
* Business class fare = 2.5 x economy. Domestic tax 5%; international tax 12% + Rs.1500 fee.
* Card payment has a 2% fee; card = 16 digits + 3-digit CVV; UPI must look like `name@bank`; cash must cover the total.
* One active booking per passenger per flight; seats are auto-allotted (1A, 1B ...); cancelling frees the seat.
* Check-in baggage limit: 25 kg economy, 40 kg business.
* A passenger or flight that already has bookings cannot be deleted.

---------------------------------------------------------------------

# Review 2 - what was added

Flow of the layers: **ui -> service -> db (JDBC) -> database**. The model, interfaces, exception and util packages are used by all of them.

## 1. Packages (8 packages)
| Package | Purpose |
|---|---|
| `airport` | `Main` entry point |
| `airport.model` | Person, Passenger, Employee, Pilot, CabinCrew, AirportStaff, Flight (+2 subclasses), Payment (+3 subclasses), Booking |
| `airport.interfaces` | all interfaces in one place |
| `airport.exception` | custom exceptions |
| `airport.db` | JDBC connection and DAOs |
| `airport.service` | business rules and statistics |
| `airport.util` | logging, CSV export, formatting, validation |
| `airport.ui` | Swing screens |

## 2. Interfaces (5)
| Interface | Implemented by | Notes |
|---|---|---|
| `Bookable` | `Flight` | abstract + **default** methods (`isFull`, `getOccupancyPercent`) + **static** method (`isValidClass`) |
| `Payable` | `Payment` (Card/UPI/Cash) | default method `getReceiptLine` |
| `CrudDAO<T>` | `PassengerDAO`, `EmployeeDAO`, `FlightDAO` | **generic** interface, `findById` returns `Optional<T>` |
| `Searchable<T>` | `PassengerDAO`, `FlightDAO`, `BookingDAO` | keyword search |
| `Exportable` | `Passenger`, `Employee`, `Flight`, `Booking` | CSV header + row |

## 3. Java library used
| Library | Where |
|---|---|
| Collections: `ArrayList`, `HashMap`, `LinkedHashMap`, `TreeMap`, `HashSet`, `Collections`/`List.of` | `ReportService`, `BookingDAO`, `MainFrame` |
| Streams, lambdas, method references, `Collectors.groupingBy/counting/summingDouble` | `ReportService`, `BookingService.getBookableFlights()` |
| `Comparator` (comparing, comparingDouble, reversed) | `ReportService.flightStats()` - five sort modes |
| `Optional` | `findById`, `findByNumber`, `orElseThrow` |
| `java.time` (`LocalDateTime`, `Duration`, `DateTimeFormatter`) | departed-flight rule, flight duration, timestamps |
| `java.io` / `java.nio.file` (`BufferedWriter`, `PrintWriter`, `FileWriter`, `Files`, `Path`) | `CsvExporter`, `FileLogHandler`, `AppLogger` |
| `java.util.logging` (`Logger`, `Handler`, `Formatter`, `ConsoleHandler`) | `AppLogger` writes `logs/airport.log` |
| `java.text.NumberFormat`, `String.format`, `StringBuilder`, `String.repeat`, regex | `FormatUtil`, `Validate`, text report |
| Generics with wildcard `List<? extends Exportable>` | `CsvExporter.export()` |

## 4. Exception handling
Hierarchy:
```
Exception
 +- AirportException                 (base, checked)
     +- ValidationException          bad user input
     +- DatabaseException            wraps SQLException (exception chaining)
     +- DuplicateRecordException     UNIQUE violated
     +- RecordNotFoundException      id does not exist
     +- RecordInUseException         foreign key blocks delete
     +- PaymentException             bad card / UPI / cash
     +- ExportException              wraps IOException
     +- InvalidBookingException
          +- SeatUnavailableException
          +- FlightDepartedException
RuntimeException
 +- ConfigurationException           UNCHECKED (missing driver / sql folder)
```
Techniques: `try / catch / finally`, multi-catch (`catch (A | B e)`), try-with-resources, `throw` and `throws`, custom checked and unchecked exceptions, exception chaining (`new X(msg, cause)`), re-throwing after logging, and a global `Thread.setDefaultUncaughtExceptionHandler` in `Main`.

## 5. New business rules (BookingService)
* A flight that has already departed cannot be booked, cancelled or checked in.
* International flights need a passport number on the passenger.
* Travel class must be ECONOMY or BUSINESS.
* Only flights with free seats that have not departed are offered on the Book Ticket screen.

## 6. New screens
* **Reports & Export**: key numbers, sortable flight report, breakdowns, and export of passengers / flights / employees / bookings to CSV and a full text report.
* **Activity Log**: shows `logs/airport.log` (bookings, cancellations, check-ins, errors, exports).



---------------------------------------------------------------------

# Version 3 - summary
See `CHANGELOG.md`. New classes: `User`, `UserDAO`, `AuthService`, `Session`, `PasswordUtil`, `AuthenticationException`
(login and roles); `SeatMapDialog`, `TicketDialog`, `HistoryDialog`, `LoginPanel`, `Toast`, `HintField`, `DateTimeSpinner` (UI).
