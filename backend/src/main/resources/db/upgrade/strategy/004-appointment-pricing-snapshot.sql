-- Additive upgrade after 003; historical appointment values are NOT backfilled.
ALTER TABLE appointments ADD COLUMN pricing_key VARCHAR(20) NULL,
 ADD COLUMN base_price DECIMAL(12,2) NULL,
 ADD COLUMN discount_rate DECIMAL(19,18) NULL;
