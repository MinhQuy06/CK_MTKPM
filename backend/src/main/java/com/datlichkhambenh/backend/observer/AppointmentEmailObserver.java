package com.datlichkhambenh.backend.observer;

import com.datlichkhambenh.backend.service.AsyncEmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Concrete Observer: báo cho bệnh nhân qua email. Chạy trong giao dịch đặt lịch, nhưng email vẫn chỉ đi
 * sau khi commit. Lỗi ở đây chỉ ghi log, không bao giờ làm hủy lịch đã đặt.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentEmailObserver {
    private final AsyncEmailNotificationService notifications;

    @EventListener
    public void onBooked(AppointmentBookedEvent event) {
        try {
            notifications.sendBookingConfirmation(event.appointment());
        } catch (Exception e) {
            log.error("Lỗi khi trigger gửi mail xác nhận đặt lịch: {}", e.getMessage());
        }
    }

    @EventListener
    public void onCancelled(AppointmentCancelledEvent event) {
        try {
            notifications.sendCancellationNotice(event.appointment());
        } catch (Exception e) {
            log.error("Lỗi khi trigger mail hủy lịch: {}", e.getMessage());
        }
    }
}
