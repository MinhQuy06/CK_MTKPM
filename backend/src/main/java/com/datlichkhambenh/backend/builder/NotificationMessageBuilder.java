package com.datlichkhambenh.backend.builder;

import com.datlichkhambenh.backend.messaging.EmailNotificationMessage;

/**
 * Builder (GoF): assembles an {@link EmailNotificationMessage} field by field, by name, instead of
 * a nine-argument constructor where two neighbouring Strings are easy to swap.
 */
public final class NotificationMessageBuilder {
    private String type;
    private String recipientEmail;
    private String patientName;
    private String bookingCode;
    private String doctorName;
    private String workDate;
    private String startTime;
    private String endTime;
    private String price;

    private NotificationMessageBuilder() {
    }

    public static NotificationMessageBuilder aMessage() {
        return new NotificationMessageBuilder();
    }

    public NotificationMessageBuilder type(String type) {
        this.type = type;
        return this;
    }

    public NotificationMessageBuilder recipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
        return this;
    }

    public NotificationMessageBuilder patientName(String patientName) {
        this.patientName = patientName;
        return this;
    }

    public NotificationMessageBuilder bookingCode(String bookingCode) {
        this.bookingCode = bookingCode;
        return this;
    }

    public NotificationMessageBuilder doctorName(String doctorName) {
        this.doctorName = doctorName;
        return this;
    }

    public NotificationMessageBuilder workDate(String workDate) {
        this.workDate = workDate;
        return this;
    }

    public NotificationMessageBuilder startTime(String startTime) {
        this.startTime = startTime;
        return this;
    }

    public NotificationMessageBuilder endTime(String endTime) {
        this.endTime = endTime;
        return this;
    }

    public NotificationMessageBuilder price(String price) {
        this.price = price;
        return this;
    }

    /** @throws IllegalStateException when type, recipientEmail or bookingCode is missing; price may be null. */
    public EmailNotificationMessage build() {
        require("type", type);
        require("recipientEmail", recipientEmail);
        require("bookingCode", bookingCode);
        return new EmailNotificationMessage(type, recipientEmail, patientName, bookingCode, doctorName,
                workDate, startTime, endTime, price);
    }

    private static void require(String field, String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Notification message is missing required field: " + field);
        }
    }
}
