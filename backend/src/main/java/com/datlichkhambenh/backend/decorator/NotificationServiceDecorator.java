package com.datlichkhambenh.backend.decorator;

import com.datlichkhambenh.backend.entity.Appointment;
import com.datlichkhambenh.backend.service.AsyncEmailNotificationService;

/**
 * Decorator (GoF): cùng interface với dịch vụ thông báo mà nó bọc, mặc định chuyển tiếp nguyên lời gọi.
 * Concrete decorator chỉ override phần hành vi thêm vào, trước hoặc sau khi chuyển tiếp.
 */
public abstract class NotificationServiceDecorator implements AsyncEmailNotificationService {
    protected final AsyncEmailNotificationService inner;

    protected NotificationServiceDecorator(AsyncEmailNotificationService inner) {
        this.inner = inner;
    }

    @Override
    public void sendBookingConfirmation(Appointment appointment) {
        inner.sendBookingConfirmation(appointment);
    }

    @Override
    public void sendAppointmentReminder(Appointment appointment) {
        inner.sendAppointmentReminder(appointment);
    }

    @Override
    public void sendCancellationNotice(Appointment appointment) {
        inner.sendCancellationNotice(appointment);
    }
}
