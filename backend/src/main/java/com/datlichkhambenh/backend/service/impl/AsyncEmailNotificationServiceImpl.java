package com.datlichkhambenh.backend.service.impl;

import com.datlichkhambenh.backend.builder.NotificationMessageDirector;
import com.datlichkhambenh.backend.entity.Appointment;
import com.datlichkhambenh.backend.messaging.EmailNotificationMessage;
import com.datlichkhambenh.backend.messaging.EmailNotificationRequestedEvent;
import com.datlichkhambenh.backend.service.AsyncEmailNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncEmailNotificationServiceImpl implements AsyncEmailNotificationService {
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationMessageDirector director;

    @Override
    public void sendBookingConfirmation(Appointment appointment) {
        publish(director.bookingConfirmed(appointment));
    }

    @Override
    public void sendAppointmentReminder(Appointment appointment) {
        publish(director.reminder(appointment));
    }

    @Override
    public void sendCancellationNotice(Appointment appointment) {
        publish(director.cancelled(appointment));
    }

    private void publish(EmailNotificationMessage message) {
        eventPublisher.publishEvent(new EmailNotificationRequestedEvent(message));
    }
}
