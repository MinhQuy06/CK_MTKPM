package com.datlichkhambenh.backend.observer;

import com.datlichkhambenh.backend.support.BookingApiFixture;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Observer tickets 06-09: booking events and the observers that react to them. */
class AppointmentObserverIntegrationTests extends BookingApiFixture {

    @Test
    void bookingSendsExactlyOneConfirmationAfterCommit() throws Exception {
        var appointment = book();

        var message = nextEmail();
        assertEquals("BOOKING_CONFIRMED", message.type());
        assertEquals(appointment.path("bookingCode").asText(), message.bookingCode());
        assertNull(noMoreEmailWithin(300), "only one confirmation expected");
    }
}
