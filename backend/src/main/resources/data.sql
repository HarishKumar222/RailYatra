-- ================================================================
-- RailYatra Sample Data
-- Runs automatically on startup (spring.sql.init.mode=always)
-- ================================================================

-- Admin user  (password = Admin@123)
INSERT INTO users (name, email, password, phone, role, is_premium, is_active)
VALUES ('Admin', 'admin@railyatra.com',
        '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQyCGVm8KApHuiDeRy8LYNby.',
        '9876543210', 'ADMIN', true, true)
ON CONFLICT (email) DO NOTHING;

-- Demo user   (password = Demo@1234)
INSERT INTO users (name, email, password, phone, role, is_premium, is_active)
VALUES ('Demo User', 'demo@railyatra.com',
        '$2a$12$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uPFkpJqC.',
        '9876543211', 'USER', false, true)
ON CONFLICT (email) DO NOTHING;

-- Trains
INSERT INTO trains (train_number, train_name, source_station, dest_station,
    departure_time, arrival_time, journey_mins,
    total_seats_sl, total_seats_3a, total_seats_2a, total_seats_1a,
    base_fare_sl, base_fare_3a, base_fare_2a, base_fare_1a,
    is_active, days_of_run)
VALUES
('12951','Mumbai Rajdhani Express','NEW DELHI','MUMBAI CENTRAL',
 '17:00','08:15',915, 0,324,192,24, 0,1450,2100,3800, true,'DAILY'),
('12005','Kalka Shatabdi Express','NEW DELHI','KALKA',
 '07:40','10:55',195, 0,0,252,56, 0,0,890,1650, true,'DAILY'),
('12259','Sealdah Duronto Express','NEW DELHI','SEALDAH',
 '20:10','11:00',890, 246,180,72,18, 720,1890,2750,4200, true,'MON,WED,FRI,SUN'),
('12909','Garib Rath Express','HAZRAT NIZAMUDDIN','BANDRA TERMINUS',
 '14:30','07:30',1020, 0,720,0,0, 0,860,0,0, true,'TUE,THU,SAT'),
('12625','Kerala Express','NEW DELHI','THIRUVANANTHAPURAM',
 '11:35','18:30',1855, 720,360,144,18, 985,2650,3800,5900, true,'DAILY'),
('11041','Chennai Mail','MUMBAI CST','CHENNAI CENTRAL',
 '21:30','16:30',1140, 900,288,96,24, 680,1750,2500,3900, true,'DAILY'),
('12123','Deccan Queen','MUMBAI CST','PUNE JUNCTION',
 '07:15','10:25',190, 0,0,336,0, 0,0,320,0, true,'DAILY'),
('12302','Howrah Rajdhani','HOWRAH','NEW DELHI',
 '13:50','10:00',1210, 0,468,192,48, 0,1680,2450,4100, true,'DAILY'),
('12649','Sampark Kranti Express','BENGALURU','HAZRAT NIZAMUDDIN',
 '20:00','06:00',1800, 1080,360,144,18, 1100,2850,4100,6200, true,'MON,WED,FRI'),
('12127','Intercity Express','MUMBAI CST','PUNE JUNCTION',
 '06:05','08:45',160, 300,0,0,0, 180,0,0,0, true,'DAILY')
ON CONFLICT (train_number) DO NOTHING;
