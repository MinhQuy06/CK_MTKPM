package com.datlichkhambenh.backend.service.impl;

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

    @Override
    public void sendBookingConfirmation(Appointment appointment) {
        publish("BOOKING_CONFIRMED", appointment);
    }

    @Override
    public void sendAppointmentReminder(Appointment appointment) {
        publish("APPOINTMENT_REMINDER", appointment);
    }

    @Override
    public void sendCancellationNotice(Appointment appointment) {
        publish("BOOKING_CANCELLED", appointment);
    }

    private void publish(String type, Appointment appointment) {
        EmailNotificationMessage message = new EmailNotificationMessage(
                type,
                appointment.getPatient().getUser().getEmail(),
                appointment.getPatient().getFullName(),
                appointment.getBookingCode(),
                appointment.getDoctor().getFullName(),
                appointment.getSchedule().getWorkDate().toString(),
                appointment.getSchedule().getStartTime().toString(),
                appointment.getSchedule().getEndTime().toString(),
                appointment.getPrice() == null ? null : appointment.getPrice().toPlainString()
        );
        eventPublisher.publishEvent(new EmailNotificationRequestedEvent(message));
    }
}
