package com.datlichkhambenh.backend.decorator;

import com.datlichkhambenh.backend.support.BookingApiFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    void bookingRecordsOneSentEmailInHistory() throws Exception {
        String code = book().path("bookingCode").asText();
        nextEmail();

        assertEquals(List.of("BOOKING_CONFIRMED EMAIL SENT"), historyOf(code));
    }
}
