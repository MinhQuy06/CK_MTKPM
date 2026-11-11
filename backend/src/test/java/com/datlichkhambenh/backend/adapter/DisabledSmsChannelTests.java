package com.datlichkhambenh.backend.adapter;

import com.datlichkhambenh.backend.builder.NotificationMessageBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** SMS tắt theo cấu hình: Adapter báo "bỏ qua", không phải lỗi. */
class DisabledSmsChannelTests {
    @Test
    void disabledGatewayYieldsSkippedNotFailed() {
        var adapter = new SmsChannelAdapter(new DisabledSmsGatewayClient());
        var message = NotificationMessageBuilder.aMessage().type("BOOKING_CONFIRMED").recipientEmail("bn@test.vn")
                .patientName("A").bookingCode("BK-1").doctorName("BS B")
                .workDate("2026-11-12").startTime("09:00").endTime("09:30").price("300000").build();

        assertEquals(DeliveryResult.Status.SKIPPED_DISABLED, adapter.send("0912345678", message).status());
    }
}
