package com.datlichkhambenh.backend.adapter;

import com.datlichkhambenh.backend.messaging.EmailNotificationMessage;

/**
 * Target (GoF Adapter): cách duy nhất phòng khám gửi thông báo lịch hẹn qua một kênh phụ.
 * Nơi gọi không bao giờ biết nhà cung cấp nào đứng phía sau.
 */
public interface NotificationChannel {

    /** Tên kênh ghi vào lịch sử thông báo, ví dụ "SMS". */
    String name();

    /**
     * @param patientPhone số điện thoại bệnh nhân như phòng khám lưu (có thể null hoặc dạng 0xxx)
     * @param message      nội dung cần báo cho bệnh nhân
     */
    DeliveryResult send(String patientPhone, EmailNotificationMessage message);
}
