package com.datlichkhambenh.backend.builder;

import com.datlichkhambenh.backend.entity.Appointment;
import com.datlichkhambenh.backend.messaging.EmailNotificationMessage;
import org.springframework.stereotype.Component;

/**
 * Director (GoF Builder): owns the recipe for each kind of appointment notification, so callers ask
 * for "a cancellation notice for this appointment" instead of setting nine fields themselves.
 */
@Component
public class NotificationMessageDirector {
    public static final String BOOKING_CONFIRMED = "BOOKING_CONFIRMED";
    public static final String APPOINTMENT_REMINDER = "APPOINTMENT_REMINDER";
    public static final String BOOKING_CANCELLED = "BOOKING_CANCELLED";

    public EmailNotificationMessage bookingConfirmed(Appointment appointment) {
        return forAppointment(BOOKING_CONFIRMED, appointment);
    }

    public EmailNotificationMessage reminder(Appointment appointment) {
        return forAppointment(APPOINTMENT_REMINDER, appointment);
    }

    public EmailNotificationMessage cancelled(Appointment appointment) {
        return forAppointment(BOOKING_CANCELLED, appointment);
    }

    private EmailNotificationMessage forAppointment(String type, Appointment appointment) {
        return NotificationMessageBuilder.aMessage()
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
    }
}
