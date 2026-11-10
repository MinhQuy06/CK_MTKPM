package com.datlichkhambenh.backend.decorator;

import com.datlichkhambenh.backend.adapter.SimulatedSmsGatewayClient;
import com.datlichkhambenh.backend.adapter.VietnamPhone;
import com.datlichkhambenh.backend.support.BookingApiFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Decorator ticket 16: a patient with a phone also gets an SMS, through the Adapter. */
class SmsNotificationIntegrationTests extends BookingApiFixture {
    @Autowired NotificationLogRepository history;
    @Autowired SimulatedSmsGatewayClient gateway;

    @Override
    protected boolean patientHasPhone() {
        return true;
    }

    @BeforeEach
    void clearGateway() {
        gateway.sent().clear();
    }

    @Test
    void patientWithPhoneGetsAnSmsAfterTheEmail() throws Exception {
        String code = book().path("bookingCode").asText();

        assertEquals("BOOKING_CONFIRMED", nextEmail().type());
        assertEquals(1, gateway.sent().size());
        var sms = gateway.sent().get(0);
        assertEquals(VietnamPhone.toInternational(patientPhone).orElseThrow(), sms.msisdn());
        assertTrue(sms.text().contains(code), sms.text());
        assertEquals(List.of("BOOKING_CONFIRMED EMAIL SENT", "BOOKING_CONFIRMED SMS SENT"),
                history.findByBookingCodeOrderByIdAsc(code).stream()
                        .map(row -> row.getType() + " " + row.getChannel() + " " + row.getStatus()).toList());
    }
}
