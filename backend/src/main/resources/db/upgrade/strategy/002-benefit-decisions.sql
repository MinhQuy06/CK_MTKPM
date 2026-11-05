-- One-time additive upgrade after 001; preserve all existing requests.
ALTER TABLE benefit_requests ADD COLUMN approved_from DATE NULL, ADD COLUMN approved_until DATE NULL;
CREATE TABLE IF NOT EXISTS benefit_decisions (
 id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
 request_id BIGINT NOT NULL,
 action VARCHAR(20) NOT NULL,
 reason VARCHAR(1000) NOT NULL,
 decided_by BIGINT NOT NULL,
 decided_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
 CONSTRAINT fk_decision_request FOREIGN KEY (request_id) REFERENCES benefit_requests(id),
 CONSTRAINT fk_decision_actor FOREIGN KEY (decided_by) REFERENCES users(id),
 INDEX idx_decision_request (request_id,id)
);
