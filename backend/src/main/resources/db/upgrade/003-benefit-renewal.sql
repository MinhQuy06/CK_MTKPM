-- One-time additive upgrade after 002. Renewal creates a NEW request.
ALTER TABLE benefit_requests ADD COLUMN renewal_of_id BIGINT NULL,
 ADD CONSTRAINT fk_benefit_renewal FOREIGN KEY (renewal_of_id) REFERENCES benefit_requests(id);
