-- Sample data (loaded only when the database is empty)

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
('AI101', 'Air India', 'Chennai',   'Delhi',     '2026-11-05 06:30', '2026-11-05 09:15', 180, 180,  5200, 'DOMESTIC',      'T1', 0, 1),
('6E245', 'IndiGo',    'Chennai',   'Mumbai',    '2026-11-05 11:00', '2026-11-05 13:05', 186, 186,  4100, 'DOMESTIC',      'T2', 0, 2),
('UK831', 'Vistara',   'Bengaluru', 'Kolkata',   '2026-11-06 14:20', '2026-11-06 16:50', 158, 158,  4800, 'DOMESTIC',      'T1', 0, 3),
('AI307', 'Air India', 'Chennai',   'Dubai',     '2026-11-08 02:10', '2026-11-08 05:00', 250, 250, 16500, 'INTERNATIONAL', NULL, 1, 2),
('SQ529', 'Singapore Airlines', 'Chennai', 'Singapore', '2026-11-09 09:45', '2026-11-09 16:30', 300, 300, 21000, 'INTERNATIONAL', NULL, 0, 3),
('BA144', 'British Airways', 'Delhi', 'London',   '2026-11-12 03:30', '2026-11-12 08:10', 280, 280, 48000, 'INTERNATIONAL', NULL, 1, 1);
