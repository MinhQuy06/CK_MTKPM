package com.datlichkhambenh.backend.decorator;

import com.datlichkhambenh.backend.builder.NotificationMessageDirector;
import com.datlichkhambenh.backend.entity.Appointment;
import com.datlichkhambenh.backend.service.AsyncEmailNotificationService;

/**
 * Concrete Decorator: a notification of the same kind for the same booking that was already e-mailed
 * is not forwarded again; the skip is recorded instead. Outermost, so a duplicate triggers nothing
 * further down the chain (no second SMS either). The history table survives restarts.
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
