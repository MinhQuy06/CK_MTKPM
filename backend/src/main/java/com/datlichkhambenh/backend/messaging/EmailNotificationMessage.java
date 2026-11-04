package com.datlichkhambenh.backend.messaging;

public record EmailNotificationMessage(
        String type,
        String recipientEmail,
        String patientName,
        String bookingCode,
        String doctorName,
        String workDate,
        String startTime,
        String endTime,
        String price
) {
}
