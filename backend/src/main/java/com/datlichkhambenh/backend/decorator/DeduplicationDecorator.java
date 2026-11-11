package com.datlichkhambenh.backend.decorator;

import com.datlichkhambenh.backend.builder.NotificationMessageDirector;
import com.datlichkhambenh.backend.entity.Appointment;
import com.datlichkhambenh.backend.service.AsyncEmailNotificationService;

/**
 * Concrete Decorator: thông báo cùng loại cho cùng lịch hẹn đã gửi email rồi thì không chuyển tiếp nữa, chỉ ghi
 * SKIPPED_DUPLICATE. Nằm ngoài cùng nên lần gửi trùng không kích hoạt gì phía trong (kể cả SMS lần hai).
 * Dựa trên bảng lịch sử nên vẫn đúng sau khi khởi động lại server.
 */
public class DeduplicationDecorator extends NotificationServiceDecorator {
    private final NotificationLogRepository history;

    public DeduplicationDecorator(AsyncEmailNotificationService inner, NotificationLogRepository history) {
        super(inner);
        this.history = history;
    }

    @Override
    public void sendBookingConfirmation(Appointment appointment) {
        if (firstTime(NotificationMessageDirector.BOOKING_CONFIRMED, appointment)) {
            inner.sendBookingConfirmation(appointment);
        }
    }

    @Override
    public void sendAppointmentReminder(Appointment appointment) {
        if (firstTime(NotificationMessageDirector.APPOINTMENT_REMINDER, appointment)) {
            inner.sendAppointmentReminder(appointment);
        }
    }

    @Override
    public void sendCancellationNotice(Appointment appointment) {
        if (firstTime(NotificationMessageDirector.BOOKING_CANCELLED, appointment)) {
            inner.sendCancellationNotice(appointment);
        }
    }

    private boolean firstTime(String type, Appointment appointment) {
        String code = appointment.getBookingCode();
        if (history.existsByTypeAndBookingCodeAndChannelAndStatus(type, code, "EMAIL", "SENT")) {
            history.save(new NotificationLog(type, code, "EMAIL", "SKIPPED_DUPLICATE", "Đã gửi trước đó"));
            return false;
        }
        return true;
    }
}
