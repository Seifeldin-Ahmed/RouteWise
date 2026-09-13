--liquibase formatted sql

--changeset fawry:006-seed-users
INSERT INTO users (email, password, full_name, role, enabled) VALUES
  ('admin@fawry.com',  '$2a$10$dUmwlO4WRMvzytIRWTsvY.PDkH7an1Wj.Ms/K1QOAWh53wNIjHBI2', 'System Administrator', 'ADMIN', TRUE),
  ('biller@fawry.com', '$2a$10$O2haDz0rhiA7O/tOWuIWGeSlROxLmg6e/4SjFInFEry/rXjGR.ek.', 'Cairo Electricity Co.', 'USER',  TRUE),
  ('water@fawry.com',  '$2a$10$O2haDz0rhiA7O/tOWuIWGeSlROxLmg6e/4SjFInFEry/rXjGR.ek.', 'Greater Cairo Water',   'USER',  TRUE);
--rollback DELETE FROM users WHERE email IN ('admin@fawry.com', 'biller@fawry.com', 'water@fawry.com');

--changeset fawry:007-seed-gateways
INSERT INTO gateways (name, fixed_commission, percentage_commission, min_transaction_amount,
                      max_transaction_amount, daily_limit_per_biller, available_from, available_to,
                      available_day_from, available_day_to, processing_time_hours, active) VALUES
  ('gateway 1', 2.00, 1.50,  10.00,  5000.00,  50000.00, '00:00', '00:00',
   'MONDAY', 'SUNDAY',   0, TRUE),
  ('gateway 2', 5.00, 0.80, 100.00,     NULL, 200000.00, '09:00', '17:00',
   'SUNDAY', 'THURSDAY', 24, TRUE),
  ('gateway 3', 0.00, 2.50,  50.00, 10000.00, 100000.00, '00:00', '00:00',
   'MONDAY', 'SUNDAY',   2, TRUE);
--rollback DELETE FROM gateways WHERE name IN ('gateway 1', 'gateway 2', 'gateway 3');
