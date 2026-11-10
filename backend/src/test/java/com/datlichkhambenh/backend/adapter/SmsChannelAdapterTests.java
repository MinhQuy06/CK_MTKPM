package com.datlichkhambenh.backend.adapter;

import com.datlichkhambenh.backend.builder.NotificationMessageBuilder;
import com.datlichkhambenh.backend.messaging.EmailNotificationMessage;
import com.datlichkhambenh.backend.support.BookingApiFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Adapter tickets 10-12: the app talks to NotificationChannel; the vendor client never leaks out. */
class SmsChannelAdapterTests extends BookingApiFixture {
    @Autowired NotificationChannel channel;
    @Autowired SimulatedSmsGatewayClient gateway;

    @BeforeEach
    void clearGateway() {
        gateway.sent().clear();
    }

    private EmailNotificationMessage confirmation() {
        return NotificationMessageBuilder.aMessage().type("BOOKING_CONFIRMED").recipientEmail("bn@test.vn")
                .patientName("Bệnh nhân A").bookingCode("BK-42").doctorName("BS B")
                .workDate("2026-11-12").startTime("09:00").endTime("09:30").price("300000").build();
    }

    @Test
    void sendingThroughTheChannelReachesTheVendorInItsOwnFormat() {
        var result = channel.send("+84912345678", confirmation());

        assertEquals(DeliveryResult.Status.SENT, result.status());
        assertEquals(1, gateway.sent().size());
        var sms = gateway.sent().get(0);
        assertEquals("+84912345678", sms.msisdn());
        assertTrue(sms.text().contains("BK-42"), sms.text());
        assertTrue(sms.text().contains("09:00"), sms.text());
    }
}
