-- =====================================================================
--  AIRPORT MANAGEMENT SYSTEM  -  DATABASE DESIGN (SQLite)
--  Tables: passenger, employee, flight, booking, payment, checkin
-- =====================================================================

CREATE TABLE IF NOT EXISTS passenger (
    passenger_id  INTEGER PRIMARY KEY AUTOINCREMENT,
    name          TEXT    NOT NULL,
    age           INTEGER NOT NULL CHECK (age > 0 AND age < 120),
    gender        TEXT    NOT NULL CHECK (gender IN ('Male','Female','Other')),
    phone         TEXT    NOT NULL,
    email         TEXT,
    passport_no   TEXT    UNIQUE,
    nationality   TEXT    NOT NULL DEFAULT 'Indian'
);

-- One table for the whole Employee hierarchy (Pilot / CabinCrew / AirportStaff)
CREATE TABLE IF NOT EXISTS employee (
    employee_id   INTEGER PRIMARY KEY AUTOINCREMENT,
    name          TEXT    NOT NULL,
    age           INTEGER NOT NULL CHECK (age >= 18 AND age < 70),
    phone         TEXT    NOT NULL,
    email         TEXT,
    salary        REAL    NOT NULL CHECK (salary >= 0),
    role          TEXT    NOT NULL CHECK (role IN ('PILOT','CABIN_CREW','AIRPORT_STAFF')),
    license_no    TEXT,      -- Pilot only
    flying_hours  INTEGER,   -- Pilot only
    crew_rank     TEXT,      -- CabinCrew only
    department    TEXT       -- AirportStaff only
);

-- One table for DomesticFlight / InternationalFlight
CREATE TABLE IF NOT EXISTS flight (
    flight_id        INTEGER PRIMARY KEY AUTOINCREMENT,
    flight_no        TEXT    NOT NULL UNIQUE,
    airline          TEXT    NOT NULL,
    origin           TEXT    NOT NULL,
    destination      TEXT    NOT NULL,
    departure_time   TEXT    NOT NULL,          -- yyyy-MM-dd HH:mm
    arrival_time     TEXT    NOT NULL,
    total_seats      INTEGER NOT NULL CHECK (total_seats > 0),
    available_seats  INTEGER NOT NULL,
    base_fare        REAL    NOT NULL CHECK (base_fare > 0),
    flight_type      TEXT    NOT NULL CHECK (flight_type IN ('DOMESTIC','INTERNATIONAL')),
    terminal         TEXT,                      -- Domestic only
    visa_required    INTEGER DEFAULT 0,         -- International only (0/1)
    pilot_id         INTEGER,
    CHECK (available_seats >= 0 AND available_seats <= total_seats),
    FOREIGN KEY (pilot_id) REFERENCES employee(employee_id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS booking (
    booking_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    passenger_id   INTEGER NOT NULL,
    flight_id      INTEGER NOT NULL,
    seat_no        TEXT    NOT NULL,
    travel_class   TEXT    NOT NULL CHECK (travel_class IN ('ECONOMY','BUSINESS')),
    fare           REAL    NOT NULL,
    booking_time   TEXT    NOT NULL,
    status         TEXT    NOT NULL DEFAULT 'CONFIRMED'
                   CHECK (status IN ('CONFIRMED','CHECKED_IN','CANCELLED')),
    refund_amount  REAL    NOT NULL DEFAULT 0,
    FOREIGN KEY (passenger_id) REFERENCES passenger(passenger_id) ON DELETE RESTRICT,
    FOREIGN KEY (flight_id)    REFERENCES flight(flight_id)       ON DELETE RESTRICT
);

-- A seat can belong to only one active booking per flight
CREATE UNIQUE INDEX IF NOT EXISTS ux_active_seat
    ON booking(flight_id, seat_no) WHERE status <> 'CANCELLED';

CREATE TABLE IF NOT EXISTS payment (
    payment_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    booking_id     INTEGER NOT NULL UNIQUE,
    method         TEXT    NOT NULL CHECK (method IN ('CARD','UPI','CASH')),
    amount         REAL    NOT NULL,            -- fare + processing fee
    paid_at        TEXT    NOT NULL,
    card_holder    TEXT,
    card_last4     TEXT,
    upi_id         TEXT,
    cash_tendered  REAL,
    FOREIGN KEY (booking_id) REFERENCES booking(booking_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS checkin (
    checkin_id     INTEGER PRIMARY KEY AUTOINCREMENT,
    booking_id     INTEGER NOT NULL UNIQUE,
    checkin_time   TEXT    NOT NULL,
    baggage_kg     REAL    NOT NULL CHECK (baggage_kg >= 0),
    gate           TEXT    NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES booking(booking_id) ON DELETE CASCADE
);

-- Login accounts (passwords are stored as salted SHA-256 hashes, never as plain text)
CREATE TABLE IF NOT EXISTS user_account (
    user_id        INTEGER PRIMARY KEY AUTOINCREMENT,
    username       TEXT    NOT NULL UNIQUE,
    password_hash  TEXT    NOT NULL,
    full_name      TEXT    NOT NULL,
    role           TEXT    NOT NULL CHECK (role IN ('ADMIN','STAFF'))
);
