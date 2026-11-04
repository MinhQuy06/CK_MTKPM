package com.datlichkhambenh.backend.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailQueueConsumer {
    private final JavaMailSender mailSender;

    @RabbitListener(queues = "${app.messaging.email-queue}")
    public void consume(EmailNotificationMessage notification) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(notification.recipientEmail());
        message.setSubject(subject(notification));
        message.setText(body(notification));
        mailSender.send(message);
        log.info("Sent {} email for booking {}", notification.type(), notification.bookingCode());
    }

    private String subject(EmailNotificationMessage notification) {
        return switch (notification.type()) {
            case "BOOKING_CANCELLED" -> "Thông báo hủy lịch khám - Mã: " + notification.bookingCode();
            case "APPOINTMENT_REMINDER" -> "Nhắc lịch khám - Mã: " + notification.bookingCode();
            default -> "Xác nhận đặt lịch khám - Mã: " + notification.bookingCode();
        };
    }

    private String body(EmailNotificationMessage n) {
        if ("BOOKING_CANCELLED".equals(n.type())) {
            return "Kính gửi %s,\n\nLịch khám mã %s đã được hủy thành công."
                    .formatted(n.patientName(), n.bookingCode());
        }
        String prefix = "APPOINTMENT_REMINDER".equals(n.type())
                ? "Đây là thông báo nhắc lịch khám của bạn."
                : "Bạn đã đặt lịch khám thành công.";
        return ("Kính gửi %s,\n\n%s\nMã đặt chỗ: %s\nBác sĩ: %s\nNgày khám: %s\n"
                + "Thời gian: %s - %s\nPhí khám: %s VNĐ")
                .formatted(n.patientName(), prefix, n.bookingCode(), n.doctorName(), n.workDate(),
                        n.startTime(), n.endTime(), n.price());
    }
}
