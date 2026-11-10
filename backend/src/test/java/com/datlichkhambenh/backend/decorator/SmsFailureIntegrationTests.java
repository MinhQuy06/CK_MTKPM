package com.datlichkhambenh.backend.decorator;

import com.datlichkhambenh.backend.adapter.SimulatedSmsGatewayClient;
import com.datlichkhambenh.backend.support.BookingApiFixture;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Decorator ticket 17: a crashing SMS vendor must not break the e-mail or the booking. */
@Import(SmsFailureIntegrationTests.CrashingVendor.class)
class SmsFailureIntegrationTests extends BookingApiFixture {
    @Autowired NotificationLogRepository history;

    @TestConfiguration
    static class CrashingVendor {
        @Bean
        @Primary
        SimulatedSmsGatewayClient crashingGateway() {
            return new SimulatedSmsGatewayClient() {
                @Override
                public int submitSms(String msisdn, String text) {
                    throw new IllegalStateException("SMS vendor timeout");
                }
            };
        }
    }

    @Override
    protected boolean patientHasPhone() {
        return true;
    }

    @Test
    void bookingAndEmailSucceedWhenSmsCrashes() throws Exception {
        String code = book().path("bookingCode").asText();

        assertEquals("BOOKING_CONFIRMED", nextEmail().type());
        assertEquals(List.of("BOOKING_CONFIRMED EMAIL SENT", "BOOKING_CONFIRMED SMS FAILED"),
                history.findByBookingCodeOrderByIdAsc(code).stream()
                        .map(row -> row.getType() + " " + row.getChannel() + " " + row.getStatus()).toList());
    }
}
