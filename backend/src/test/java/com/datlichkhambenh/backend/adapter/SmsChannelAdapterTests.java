package com.datlichkhambenh.backend.adapter;

import com.datlichkhambenh.backend.builder.NotificationMessageBuilder;
import com.datlichkhambenh.backend.messaging.EmailNotificationMessage;
import com.datlichkhambenh.backend.support.BookingApiFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
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

    @Test
    void localNumberIsConvertedBeforeReachingTheVendor() {
        var result = channel.send("0912 345 678", confirmation());

        assertEquals(DeliveryResult.Status.SENT, result.status());
        assertEquals("+84912345678", gateway.sent().get(0).msisdn());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", "   "})
    void patientWithoutPhoneIsSkippedWithoutCallingTheVendor(String phone) {
        var result = channel.send(phone, confirmation());

        assertEquals(DeliveryResult.Status.SKIPPED_NO_PHONE, result.status());
        assertTrue(gateway.sent().isEmpty());
    }

    @Test
    void malformedNumberFailsWithoutCallingTheVendor() {
        var result = channel.send("12345", confirmation());

        assertEquals(DeliveryResult.Status.FAILED, result.status());
        assertTrue(gateway.sent().isEmpty());
    }
}
