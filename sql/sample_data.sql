-- Sample data (loaded only when the database is empty).
-- Flight dates are relative to the day the database is created, so the demo flights are always in the future.

INSERT INTO employee (name, age, phone, email, salary, role, license_no, flying_hours, crew_rank, department) VALUES
('Capt. Rajesh Menon', 45, '9840011122', 'rajesh.menon@airline.in', 450000, 'PILOT', 'ATPL-IN-20418', 9200, NULL, NULL),
('Capt. Anita Verma',  39, '9840033344', 'anita.verma@airline.in',  420000, 'PILOT', 'ATPL-IN-31877', 6400, NULL, NULL),
('Capt. Sameer Khan',  42, '9840055566', 'sameer.khan@airline.in',  435000, 'PILOT', 'ATPL-IN-27650', 7800, NULL, NULL),
('Priya Nair',         28, '9791122334', 'priya.nair@airline.in',    85000, 'CABIN_CREW', NULL, NULL, 'Purser', NULL),
('Arjun Das',          26, '9791155667', 'arjun.das@airline.in',     62000, 'CABIN_CREW', NULL, NULL, 'Flight Attendant', NULL),
('Kavya Iyer',         30, '9791188990', 'kavya.iyer@airline.in',    70000, 'CABIN_CREW', NULL, NULL, 'Senior Attendant', NULL),
('Mohan Reddy',        35, '9884412345', 'mohan.reddy@airport.in',   48000, 'AIRPORT_STAFF', NULL, NULL, NULL, 'Ground Handling'),
('Sneha Pillai',       29, '9884467890', 'sneha.pillai@airport.in',  52000, 'AIRPORT_STAFF', NULL, NULL, NULL, 'Check-in Counter');

INSERT INTO passenger (name, age, gender, phone, email, passport_no, nationality) VALUES
('Aarav Sharma',  29, 'Male',   '9876543210', 'aarav.sharma@mail.com',  'N1234567', 'Indian'),
('Diya Patel',    34, 'Female', '9812345678', 'diya.patel@mail.com',    'P7654321', 'Indian'),
('Rohan Gupta',   41, 'Male',   '9898989898', 'rohan.gupta@mail.com',   'K1122334', 'Indian'),
('Ishita Rao',    24, 'Female', '9700112233', 'ishita.rao@mail.com',    'M9988776', 'Indian'),
('James Carter',  52, 'Male',   '9001234567', 'james.carter@mail.com',  'US5566778', 'American');

INSERT INTO flight (flight_no, airline, origin, destination, departure_time, arrival_time, total_seats, available_seats, base_fare, flight_type, terminal, visa_required, pilot_id) VALUES
('AI101', 'Air India', 'Chennai',   'Delhi',     strftime('%Y-%m-%d', 'now', '+3 days') || ' 06:30', strftime('%Y-%m-%d', 'now', '+3 days') || ' 09:15', 180, 178,  5200, 'DOMESTIC',      'T1', 0, 1),
('6E245', 'IndiGo',    'Chennai',   'Mumbai',    strftime('%Y-%m-%d', 'now', '+3 days') || ' 11:00', strftime('%Y-%m-%d', 'now', '+3 days') || ' 13:05', 186, 185,  4100, 'DOMESTIC',      'T2', 0, 2),
('UK831', 'Vistara',   'Bengaluru', 'Kolkata',   strftime('%Y-%m-%d', 'now', '+4 days') || ' 14:20', strftime('%Y-%m-%d', 'now', '+4 days') || ' 16:50', 158, 158,  4800, 'DOMESTIC',      'T1', 0, 3),
('AI307', 'Air India', 'Chennai',   'Dubai',     strftime('%Y-%m-%d', 'now', '+6 days') || ' 02:10', strftime('%Y-%m-%d', 'now', '+6 days') || ' 05:00', 250, 249, 16500, 'INTERNATIONAL', NULL, 1, 2),
('SQ529', 'Singapore Airlines', 'Chennai', 'Singapore', strftime('%Y-%m-%d', 'now', '+7 days') || ' 09:45', strftime('%Y-%m-%d', 'now', '+7 days') || ' 16:30', 300, 300, 21000, 'INTERNATIONAL', NULL, 0, 3),
('BA144', 'British Airways', 'Delhi', 'London',   strftime('%Y-%m-%d', 'now', '+10 days') || ' 03:30', strftime('%Y-%m-%d', 'now', '+10 days') || ' 08:10', 280, 280, 48000, 'INTERNATIONAL', NULL, 1, 1);

-- A few existing bookings so the reports have data (available_seats above already reflects them)
INSERT INTO booking (passenger_id, flight_id, seat_no, travel_class, fare, booking_time, status, refund_amount) VALUES
(1, 1, '4A', 'ECONOMY',  5460.00,  strftime('%Y-%m-%d %H:%M', 'now'), 'CONFIRMED',  0),
(2, 1, '1A', 'BUSINESS', 13650.00, strftime('%Y-%m-%d %H:%M', 'now'), 'CHECKED_IN', 0),
(3, 2, '4A', 'ECONOMY',  4305.00,  strftime('%Y-%m-%d %H:%M', 'now'), 'CONFIRMED',  0),
(4, 4, '4A', 'ECONOMY',  19980.00, strftime('%Y-%m-%d %H:%M', 'now'), 'CONFIRMED',  0),
(5, 4, '1A', 'BUSINESS', 47700.00, strftime('%Y-%m-%d %H:%M', 'now'), 'CANCELLED',  42930.00);

INSERT INTO payment (booking_id, method, amount, paid_at, card_holder, card_last4, upi_id, cash_tendered) VALUES
(1, 'CARD', 5569.20,  strftime('%Y-%m-%d %H:%M', 'now'), 'Aarav Sharma', '4242', NULL, NULL),
(2, 'UPI',  13650.00, strftime('%Y-%m-%d %H:%M', 'now'), NULL, NULL, 'diya.patel@okaxis', NULL),
(3, 'CASH', 4305.00,  strftime('%Y-%m-%d %H:%M', 'now'), NULL, NULL, NULL, 5000.00),
(4, 'CARD', 20379.60, strftime('%Y-%m-%d %H:%M', 'now'), 'Ishita Rao', '1111', NULL, NULL),
(5, 'CARD', 48654.00, strftime('%Y-%m-%d %H:%M', 'now'), 'James Carter', '0005', NULL, NULL);

INSERT INTO checkin (booking_id, checkin_time, baggage_kg, gate) VALUES
(2, strftime('%Y-%m-%d %H:%M', 'now'), 18.5, 'G3');
