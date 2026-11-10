package com.datlichkhambenh.backend.adapter;

/** Outcome of sending through a {@link NotificationChannel}. */
public record DeliveryResult(Status status, String detail) {

    public enum Status { SENT, SKIPPED_NO_PHONE, FAILED }

    public static DeliveryResult sent(String detail) {
        return new DeliveryResult(Status.SENT, detail);
    }

    public static DeliveryResult skippedNoPhone() {
        return new DeliveryResult(Status.SKIPPED_NO_PHONE, "Bệnh nhân chưa có số điện thoại");
    }

    public static DeliveryResult failed(String detail) {
        return new DeliveryResult(Status.FAILED, detail);
    }
}
