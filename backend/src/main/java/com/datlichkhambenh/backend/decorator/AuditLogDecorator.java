package com.datlichkhambenh.backend.decorator;

import com.datlichkhambenh.backend.builder.NotificationMessageDirector;
import com.datlichkhambenh.backend.entity.Appointment;
import com.datlichkhambenh.backend.service.AsyncEmailNotificationService;

/**
 * Concrete Decorator: ghi một dòng lịch sử cho mỗi email chuyển tiếp, SENT hoặc FAILED.
 * SENT nghĩa là đã giao cho hàng đợi email; hàng đợi vẫn chỉ gửi sau khi giao dịch commit.
 */
public class AuditLogDecorator extends NotificationServiceDecorator {
    private final NotificationLogRepository history;

    public AuditLogDecorator(AsyncEmailNotificationService inner, NotificationLogRepository history) {
        super(inner);
        this.history = history;
    }

    @Override
    public void sendBookingConfirmation(Appointment appointment) {
        audited(NotificationMessageDirector.BOOKING_CONFIRMED, appointment, () -> inner.sendBookingConfirmation(appointment));
    }

    @Override
    public void sendAppointmentReminder(Appointment appointment) {
        audited(NotificationMessageDirector.APPOINTMENT_REMINDER, appointment, () -> inner.sendAppointmentReminder(appointment));
    }

    @Override
    public void sendCancellationNotice(Appointment appointment) {
        audited(NotificationMessageDirector.BOOKING_CANCELLED, appointment, () -> inner.sendCancellationNotice(appointment));
    }

    private void audited(String type, Appointment appointment, Runnable send) {
        try {
            send.run();
        } catch (RuntimeException e) {
            history.save(new NotificationLog(type, appointment.getBookingCode(), "EMAIL", "FAILED", e.getMessage()));
            throw e;
        }
        history.save(new NotificationLog(type, appointment.getBookingCode(), "EMAIL", "SENT", null));
    }
}
