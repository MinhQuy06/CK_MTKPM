package com.datlichkhambenh.backend.service.impl;

import com.datlichkhambenh.backend.builder.NotificationMessageBuilder;
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
        EmailNotificationMessage message = NotificationMessageBuilder.aMessage()
                .type(type)
                .recipientEmail(appointment.getPatient().getUser().getEmail())
                .patientName(appointment.getPatient().getFullName())
                .bookingCode(appointment.getBookingCode())
                .doctorName(appointment.getDoctor().getFullName())
                .workDate(appointment.getSchedule().getWorkDate().toString())
                .startTime(appointment.getSchedule().getStartTime().toString())
                .endTime(appointment.getSchedule().getEndTime().toString())
                .price(appointment.getPrice() == null ? null : appointment.getPrice().toPlainString())
                .build();
        eventPublisher.publishEvent(new EmailNotificationRequestedEvent(message));
    }
}
