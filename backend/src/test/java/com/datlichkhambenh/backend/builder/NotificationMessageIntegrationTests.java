package com.datlichkhambenh.backend.builder;

import com.datlichkhambenh.backend.support.BookingApiFixture;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Builder ticket 03: the booking e-mail still carries all nine fields, now assembled by the builder. */
class NotificationMessageIntegrationTests extends BookingApiFixture {

    @Test
    void bookingConfirmationCarriesEveryField() throws Exception {
        var appointment = book();

        var message = nextEmail();
        assertEquals("BOOKING_CONFIRMED", message.type());
        assertEquals(patientEmail, message.recipientEmail());
        assertEquals("Pattern Patient", message.patientName());
        assertEquals(appointment.path("bookingCode").asText(), message.bookingCode());
        assertEquals("Pattern Doctor", message.doctorName());
        assertEquals(day.toString(), message.workDate());
        assertEquals("09:00", message.startTime());
        assertEquals("09:30", message.endTime());
        assertEquals(0, new BigDecimal(message.price()).compareTo(FEE));
    }

    @Test
    void cancellationNoticeIsBuiltForTheSameAppointment() throws Exception {
        var appointment = book();
        nextEmail();

        cancel(appointment.path("id").asLong());

        var message = nextEmail();
        assertEquals("BOOKING_CANCELLED", message.type());
        assertEquals(patientEmail, message.recipientEmail());
        assertEquals(appointment.path("bookingCode").asText(), message.bookingCode());
        assertEquals(day.toString(), message.workDate());
        assertEquals("09:00", message.startTime());
    }
}
