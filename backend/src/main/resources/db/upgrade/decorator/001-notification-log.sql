-- Decorator: notification history (Hibernate ddl-auto creates it automatically; this file is for
-- manual MySQL upgrades, matching the entity NotificationLog).
CREATE TABLE IF NOT EXISTS notification_log (
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    type         VARCHAR(40)  NOT NULL,
    booking_code VARCHAR(50)  NOT NULL,
    channel      VARCHAR(10)  NOT NULL,
    status       VARCHAR(30)  NOT NULL,
    note         VARCHAR(255) NULL,
    created_at   DATETIME(6)  NOT NULL,
    INDEX idx_notification_log_booking (booking_code, type)
);
