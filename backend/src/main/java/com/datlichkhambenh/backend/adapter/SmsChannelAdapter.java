package com.datlichkhambenh.backend.adapter;

import com.datlichkhambenh.backend.messaging.EmailNotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adapter (GoF, object adapter): makes the vendor's SMS client look like a {@link NotificationChannel}.
 * It holds the client (composition) and translates our call into the vendor's shape: notification
 * message to short SMS text, and the vendor status code back into a {@link DeliveryResult}.
 */
@Component
@RequiredArgsConstructor
public class SmsChannelAdapter implements NotificationChannel {
    private final SimulatedSmsGatewayClient gateway;

    @Override
    public String name() {
        return "SMS";
    }

    @Override
    public DeliveryResult send(String patientPhone, EmailNotificationMessage message) {
        var msisdn = VietnamPhone.toInternational(patientPhone);
        if (msisdn.isEmpty()) {
            return DeliveryResult.failed("Số điện thoại không hợp lệ: " + patientPhone);
        }
        int code = gateway.submitSms(msisdn.get(), text(message));
        return code == SimulatedSmsGatewayClient.OK
                ? DeliveryResult.sent(msisdn.get())
                : DeliveryResult.failed("Nhà cung cấp SMS từ chối, mã " + code);
    }

    /** SMS is short and plain: what happened, booking code, when, with whom. */
    static String text(EmailNotificationMessage m) {
        String what = switch (m.type()) {
            case "BOOKING_CONFIRMED" -> "Da dat lich kham";
            case "BOOKING_CANCELLED" -> "Da huy lich kham";
            case "APPOINTMENT_REMINDER" -> "Nhac lich kham";
            default -> "Thong bao lich kham";
        };
        return what + " " + m.bookingCode() + ": " + m.workDate() + " " + m.startTime() + "-" + m.endTime()
                + ", " + m.doctorName() + ".";
    }
}
