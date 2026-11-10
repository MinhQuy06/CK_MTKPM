package com.datlichkhambenh.backend.builder;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Builder ticket 04: required fields are rejected at build time, optional price is allowed. */
class NotificationMessageBuilderTests {

    private NotificationMessageBuilder complete() {
        return NotificationMessageBuilder.aMessage()
                .type("BOOKING_CONFIRMED").recipientEmail("bn@test.vn").patientName("BN")
                .bookingCode("BK-1").doctorName("BS").workDate("2026-11-12")
                .startTime("09:00").endTime("09:30").price("300000");
    }

    @Test
    void missingTypeIsRejected() {
        var error = assertThrows(IllegalStateException.class, () -> complete().type(null).build());
        assertTrue(error.getMessage().contains("type"));
    }

    @Test
    void blankRecipientEmailIsRejected() {
        var error = assertThrows(IllegalStateException.class, () -> complete().recipientEmail(" ").build());
        assertTrue(error.getMessage().contains("recipientEmail"));
    }

    @Test
    void missingBookingCodeIsRejected() {
        var error = assertThrows(IllegalStateException.class, () -> complete().bookingCode(null).build());
        assertTrue(error.getMessage().contains("bookingCode"));
    }

    @Test
    void priceIsOptionalForLegacyAppointments() {
        assertNull(complete().price(null).build().price());
    }
}
