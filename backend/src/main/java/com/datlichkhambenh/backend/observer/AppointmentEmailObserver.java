package com.datlichkhambenh.backend.observer;

import com.datlichkhambenh.backend.service.AsyncEmailNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Concrete Observer: tells the patient by e-mail. It runs inside the booking transaction, but the
 * e-mail itself still only leaves after commit. A failure here never rolls the booking back.
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
