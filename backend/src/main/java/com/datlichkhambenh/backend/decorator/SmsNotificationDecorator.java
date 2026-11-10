package com.datlichkhambenh.backend.decorator;

import com.datlichkhambenh.backend.adapter.DeliveryResult;
import com.datlichkhambenh.backend.adapter.NotificationChannel;
import com.datlichkhambenh.backend.builder.NotificationMessageDirector;
import com.datlichkhambenh.backend.entity.Appointment;
import com.datlichkhambenh.backend.messaging.EmailNotificationMessage;
import com.datlichkhambenh.backend.service.AsyncEmailNotificationService;

/**
 * Concrete Decorator: sau khi dịch vụ bên trong gửi email, gửi thêm cùng thông báo qua một
 * {@link NotificationChannel} (Adapter SMS) và ghi kết quả vào lịch sử.
 */
public class SmsNotificationDecorator extends NotificationServiceDecorator {
    private final NotificationChannel channel;
    private final NotificationMessageDirector director;
    private final NotificationLogRepository history;

    public SmsNotificationDecorator(AsyncEmailNotificationService inner, NotificationChannel channel,
                                    NotificationMessageDirector director, NotificationLogRepository history) {
        super(inner);
        this.channel = channel;
        this.director = director;
        this.history = history;
    }

    @Override
    public void sendBookingConfirmation(Appointment appointment) {
        inner.sendBookingConfirmation(appointment);
        alsoSend(appointment, director.bookingConfirmed(appointment));
    }

    @Override
    public void sendAppointmentReminder(Appointment appointment) {
        inner.sendAppointmentReminder(appointment);
        alsoSend(appointment, director.reminder(appointment));
    }

    @Override
    public void sendCancellationNotice(Appointment appointment) {
        inner.sendCancellationNotice(appointment);
        alsoSend(appointment, director.cancelled(appointment));
    }

    /** SMS là kênh phụ: lỗi chỉ được ghi lại, không bao giờ ném ngược vào luồng email / đặt lịch. */
    private void alsoSend(Appointment appointment, EmailNotificationMessage message) {
        DeliveryResult result;
        try {
            result = channel.send(appointment.getPatient().getPhone(), message);
        } catch (RuntimeException e) {
            result = DeliveryResult.failed("Lỗi kênh " + channel.name() + ": " + e.getMessage());
        }
        history.save(new NotificationLog(message.type(), message.bookingCode(), channel.name(),
                result.status().name(), result.detail()));
    }
}
