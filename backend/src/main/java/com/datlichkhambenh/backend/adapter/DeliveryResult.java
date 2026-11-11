package com.datlichkhambenh.backend.adapter;

/** Kết quả gửi qua một {@link NotificationChannel}: đã gửi, bỏ qua (không có số hoặc kênh đang tắt), hoặc lỗi. */
public record DeliveryResult(Status status, String detail) {

    public enum Status { SENT, SKIPPED_NO_PHONE, SKIPPED_DISABLED, FAILED }

    public static DeliveryResult sent(String detail) {
        return new DeliveryResult(Status.SENT, detail);
    }

    public static DeliveryResult skippedNoPhone() {
        return new DeliveryResult(Status.SKIPPED_NO_PHONE, "Bệnh nhân chưa có số điện thoại");
    }

    public static DeliveryResult skippedDisabled() {
        return new DeliveryResult(Status.SKIPPED_DISABLED, "Kênh SMS đang tắt (SMS_ENABLED=false)");
    }

    public static DeliveryResult failed(String detail) {
        return new DeliveryResult(Status.FAILED, detail);
    }
}
