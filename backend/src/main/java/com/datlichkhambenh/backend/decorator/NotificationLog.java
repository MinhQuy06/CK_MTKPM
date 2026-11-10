package com.datlichkhambenh.backend.decorator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * One notification attempt: which kind, for which booking, over which channel, and what happened.
 * Status is SENT (handed to the channel/queue), FAILED, SKIPPED_DUPLICATE or SKIPPED_NO_PHONE.
 */
@Entity
@Table(name = "notification_log", indexes = @Index(name = "idx_notification_log_booking", columnList = "booking_code, type"))
@Getter
@NoArgsConstructor
public class NotificationLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String type;

    @Column(name = "booking_code", nullable = false, length = 50)
    private String bookingCode;

    @Column(nullable = false, length = 10)
    private String channel;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(length = 255)
    private String note;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    public NotificationLog(String type, String bookingCode, String channel, String status, String note) {
        this.type = type;
        this.bookingCode = bookingCode;
        this.channel = channel;
        this.status = status;
        this.note = note == null || note.length() <= 255 ? note : note.substring(0, 255);
    }

    @PrePersist
    void stamp() {
        createdAt = LocalDateTime.now();
    }
}
