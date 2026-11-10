package com.datlichkhambenh.backend.observer;

import com.datlichkhambenh.backend.support.BookingApiFixture;
import org.junit.jupiter.api.Test;

import java.util.List;

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

    @Test
    void bookingEvictsTheBookedDoctorsSlotCacheOnce() throws Exception {
        book();

        assertEquals(List.of(doctorId + "@" + day), cache.slotEvictions);
    }

    @Test
    void cancellingSendsOneNoticeAndEvictsTheSlotCache() throws Exception {
        var appointment = book();
        nextEmail();
        cache.slotEvictions.clear();

        cancel(appointment.path("id").asLong());

        assertEquals("BOOKING_CANCELLED", nextEmail().type());
        assertNull(noMoreEmailWithin(300), "only one cancellation notice expected");
        assertEquals(List.of(doctorId + "@" + day), cache.slotEvictions);
    }
}
