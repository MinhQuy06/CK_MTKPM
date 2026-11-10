package com.datlichkhambenh.backend.decorator;

import com.datlichkhambenh.backend.repository.AppointmentRepository;
import com.datlichkhambenh.backend.service.AsyncEmailNotificationService;
import com.datlichkhambenh.backend.support.BookingApiFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Decorator tickets 14-18, observed through the API and the notification history table. */
class NotificationDecoratorIntegrationTests extends BookingApiFixture {
    @Autowired NotificationLogRepository history;

    /** History rows of one booking as "TYPE CHANNEL STATUS", oldest first. */
    List<String> historyOf(String bookingCode) {
        return history.findByBookingCodeOrderByIdAsc(bookingCode).stream()
                .map(row -> row.getType() + " " + row.getChannel() + " " + row.getStatus())
                .toList();
    }

    @Test
    void bookingRecordsTheEmailAndSkipsSmsForPatientWithoutPhone() throws Exception {
        String code = book().path("bookingCode").asText();

        assertEquals("BOOKING_CONFIRMED", nextEmail().type());
        assertEquals(List.of("BOOKING_CONFIRMED EMAIL SENT", "BOOKING_CONFIRMED SMS SKIPPED_NO_PHONE"),
                historyOf(code));
    }

    @Autowired AsyncEmailNotificationService notifications;
    @Autowired AppointmentRepository appointments;

    /** Seam 2: no API sends reminders yet, so call the decorated bean the way a scheduler would. */
    void remind(long appointmentId) {
        new TransactionTemplate(tm).executeWithoutResult(tx ->
                notifications.sendAppointmentReminder(appointments.findById(appointmentId).orElseThrow()));
    }

    @Test
    void duplicateReminderIsSkippedAndRecorded() throws Exception {
        var appointment = book();
        String code = appointment.path("bookingCode").asText();
        nextEmail();

        remind(appointment.path("id").asLong());
        remind(appointment.path("id").asLong());

        assertEquals("APPOINTMENT_REMINDER", nextEmail().type());
        assertNull(noMoreEmailWithin(300), "the duplicate reminder must not be e-mailed");
        assertEquals(List.of(
                "BOOKING_CONFIRMED EMAIL SENT",
                "BOOKING_CONFIRMED SMS SKIPPED_NO_PHONE",
                "APPOINTMENT_REMINDER EMAIL SENT",
                "APPOINTMENT_REMINDER SMS SKIPPED_NO_PHONE",
                "APPOINTMENT_REMINDER EMAIL SKIPPED_DUPLICATE"), historyOf(code));
    }
}
