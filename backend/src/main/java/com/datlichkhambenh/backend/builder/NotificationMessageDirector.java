package com.datlichkhambenh.backend.builder;

import com.datlichkhambenh.backend.entity.Appointment;
import com.datlichkhambenh.backend.messaging.EmailNotificationMessage;
import org.springframework.stereotype.Component;

/**
 * Director (GoF Builder): giữ "công thức" cho từng loại thông báo lịch hẹn, để nơi gọi chỉ cần xin
 * "bản tin hủy cho lịch hẹn này" thay vì tự đặt 9 trường.
 */
@Component
public class NotificationMessageDirector {
    public static final String BOOKING_CONFIRMED = "BOOKING_CONFIRMED";
    public static final String APPOINTMENT_REMINDER = "APPOINTMENT_REMINDER";
    public static final String BOOKING_CANCELLED = "BOOKING_CANCELLED";

    public EmailNotificationMessage bookingConfirmed(Appointment appointment) {
        return forAppointment(BOOKING_CONFIRMED, appointment);
    }

    public EmailNotificationMessage reminder(Appointment appointment) {
        return forAppointment(APPOINTMENT_REMINDER, appointment);
    }

    public EmailNotificationMessage cancelled(Appointment appointment) {
        return forAppointment(BOOKING_CANCELLED, appointment);
    }

    private EmailNotificationMessage forAppointment(String type, Appointment appointment) {
        return NotificationMessageBuilder.aMessage()
                .type(type)
                .recipientEmail(appointment.getPatient().getUser().getEmail())
                .patientName(appointment.getPatient().getFullName())
                .bookingCode(appointment.getBookingCode())
                .doctorName(appointment.getDoctor().getFullName())
                .workDate(appointment.getSchedule().getWorkDate().toString())
                .startTime(appointment.getSchedule().getStartTime().toString())
                .endTime(appointment.getSchedule().getEndTime().toString())
                .price(appointment.getPrice() == null ? null : appointment.getPrice().toPlainString())
                .build();
    }
}
