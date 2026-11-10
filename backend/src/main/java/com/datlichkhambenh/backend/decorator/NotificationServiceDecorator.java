package com.datlichkhambenh.backend.decorator;

import com.datlichkhambenh.backend.entity.Appointment;
import com.datlichkhambenh.backend.service.AsyncEmailNotificationService;

/**
 * Decorator (GoF): has the same interface as the notification service it wraps and forwards to it
 * by default. Concrete decorators override only what they add, before or after forwarding.
 */
public abstract class NotificationServiceDecorator implements AsyncEmailNotificationService {
    protected final AsyncEmailNotificationService inner;

    protected NotificationServiceDecorator(AsyncEmailNotificationService inner) {
        this.inner = inner;
    }

    @Override
    public void sendBookingConfirmation(Appointment appointment) {
        inner.sendBookingConfirmation(appointment);
    }

    @Override
    public void sendAppointmentReminder(Appointment appointment) {
        inner.sendAppointmentReminder(appointment);
    }

    @Override
    public void sendCancellationNotice(Appointment appointment) {
        inner.sendCancellationNotice(appointment);
    }
}
