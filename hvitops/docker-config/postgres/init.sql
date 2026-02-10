-- Create user and database
--ALTER USER hvitops_user WITH PASSWORD 'hvitops_password';
--CREATE DATABASE hvitops_appointments OWNER hvitops_user;
GRANT ALL PRIVILEGES ON DATABASE hvitops_appointments TO hvitops_user;

\c hvitops_appointments;

-- Create appointments table
CREATE TABLE appointments (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    scheduled_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'SCHEDULED',
    notes TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_appointments_patient_id ON appointments(patient_id);
CREATE INDEX idx_appointments_doctor_id ON appointments(doctor_id);
CREATE INDEX idx_appointments_status ON appointments(status);

-- Insert seed data
INSERT INTO appointments (patient_id, doctor_id, scheduled_at, status, notes,created_at, updated_at) VALUES
(1, 101, '2026-01-25 10:00:00', 'SCHEDULED', 'Regular checkup',NOW(),NOW()),
(2, 102, '2026-01-26 14:30:00', 'SCHEDULED', 'Follow-up consultation', NOW(),NOW()),
(3, 101, '2026-01-27 09:15:00', 'COMPLETED', 'Annual physical examination', NOW(),NOW()),
(1, 102, '2026-01-28 16:00:00', 'SCHEDULED', 'Blood pressure monitoring', NOW(),NOW());
