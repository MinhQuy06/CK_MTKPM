package com.datlichkhambenh.backend.observer;

import com.datlichkhambenh.backend.support.BookingApiFixture;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void changedPriceIsRejectedWithoutAnyReaction() throws Exception {
        var body = confirmBody();
        body.put("expectedPrice", 1);

        var response = confirm(body);

        assertEquals(409, response.getStatus());
        assertEquals("PRICE_CHANGED", code(response));
        assertNoReaction();
    }

    @Test
    void unavailableBenefitIsRejectedWithoutAnyReaction() throws Exception {
        var body = confirmBody();
        body.put("strategyKey", "INSURANCE");
        body.put("expectedDiscountRate", 0.8);
        body.put("expectedPrice", 60000);

        var response = confirm(body);

        assertEquals(403, response.getStatus());
        assertEquals("BENEFIT_UNAVAILABLE", code(response));
        assertNoReaction();
    }

    @Test
    void takenSlotIsRejectedWithoutASecondReaction() throws Exception {
        book();
        nextEmail();
        cache.slotEvictions.clear();

        var response = confirm(confirmBody());

        assertEquals(409, response.getStatus());
        assertEquals("SLOT_UNAVAILABLE", code(response));
        assertNoReaction();
    }

    private void assertNoReaction() throws InterruptedException {
        assertNull(noMoreEmailWithin(300), "a rejected booking must not e-mail");
        assertTrue(cache.slotEvictions.isEmpty(), "a rejected booking must not touch the slot cache");
    }
}
