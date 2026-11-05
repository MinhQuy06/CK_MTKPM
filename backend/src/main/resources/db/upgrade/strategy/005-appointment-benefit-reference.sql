-- Additive link to the verified grant. Never backfill historical benefit references.
ALTER TABLE appointments ADD COLUMN benefit_request_id BIGINT NULL,
 ADD CONSTRAINT fk_appointment_benefit FOREIGN KEY (benefit_request_id) REFERENCES benefit_requests(id);
