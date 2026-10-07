# Airport Management System  (Java Swing + JDBC + SQLite)

Project Review 1 - GUI Design, Database Design, JDBC.

## How to run
Needs JDK 11 or newer (`java -version`, `javac -version`).

* Windows: double-click `run.bat`
* Linux / macOS: `./run.sh`
* Any IDE (VS Code / IntelliJ / Eclipse): add `lib/sqlite-jdbc.jar` to the classpath, mark `src` as the source folder, run `airport.Main`. **Run from the project root folder** (the app reads `sql/schema.sql`).

On first run the app creates `airport.db` with all tables and sample data. Delete `airport.db` to reset.

## Folder layout
```
AirportManagement/
 |- lib/sqlite-jdbc.jar          JDBC driver
 |- sql/schema.sql               DATABASE DESIGN (6 tables, keys, checks)
 |- sql/sample_data.sql          demo flights, passengers, employees
 |- src/airport/Main.java
 |- src/airport/ui/              GUI DESIGN  (Swing screens)
 |- src/airport/db/              JDBC        (connection + 4 DAO classes)
 |- src/airport/model/           OOP classes
 |- src/airport/exception/       custom exceptions
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
