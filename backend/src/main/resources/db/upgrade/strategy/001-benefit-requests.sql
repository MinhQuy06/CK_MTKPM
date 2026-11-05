-- Additive MySQL upgrade. Run once against the existing clinic database before deploying ticket #9.
CREATE TABLE IF NOT EXISTS benefit_requests (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 patient_id BIGINT NOT NULL,
 kind VARCHAR(20) NOT NULL,
 reference_code VARCHAR(120) NOT NULL,
 requested_from DATE NOT NULL,
 requested_until DATE NOT NULL,
 status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
 created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
 version BIGINT NOT NULL DEFAULT 0,
 CONSTRAINT fk_benefit_patient FOREIGN KEY (patient_id) REFERENCES patients(user_id),
 INDEX idx_benefit_patient (patient_id,created_at)
);
