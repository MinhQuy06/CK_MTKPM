package com.datlichkhambenh.backend.adapter;

import com.datlichkhambenh.backend.messaging.EmailNotificationMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Adapter (GoF, object adapter): làm client SMS của nhà cung cấp trông như một {@link NotificationChannel}.
 * Giữ client bên trong (composition) và dịch qua lại: bản tin thông báo → SMS ngắn,
 * mã trạng thái của nhà cung cấp → {@link DeliveryResult}.
 */
@Component
@RequiredArgsConstructor
public class SmsChannelAdapter implements NotificationChannel {
    private final SmsGatewayClient gateway;

    @Override
    public String name() {
        return "SMS";
    }

    @Override
    public DeliveryResult send(String patientPhone, EmailNotificationMessage message) {
        if (patientPhone == null || patientPhone.isBlank()) {
            return DeliveryResult.skippedNoPhone();
        }
        var msisdn = VietnamPhone.toInternational(patientPhone);
        if (msisdn.isEmpty()) {
            return DeliveryResult.failed("Số điện thoại không hợp lệ: " + patientPhone);
        }
        int code = gateway.submitSms(msisdn.get(), text(message));
        if (code == SmsGatewayClient.DISABLED) {
            return DeliveryResult.skippedDisabled();
        }
        return code == SmsGatewayClient.OK
                ? DeliveryResult.sent(msisdn.get())
                : DeliveryResult.failed("Nhà cung cấp SMS từ chối, mã " + code);
    }

    /** SMS ngắn, không dấu: chuyện gì, mã lịch hẹn, lúc nào, với bác sĩ nào. */
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
