package com.datlichkhambenh.backend.decorator;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationLogRepository extends JpaRepository<NotificationLog, Long> {
    List<NotificationLog> findByBookingCodeOrderByIdAsc(String bookingCode);

    boolean existsByTypeAndBookingCodeAndChannelAndStatus(String type, String bookingCode, String channel, String status);
}
