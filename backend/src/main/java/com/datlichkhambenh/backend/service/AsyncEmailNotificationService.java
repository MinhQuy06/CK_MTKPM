package com.datlichkhambenh.backend.service;

import com.datlichkhambenh.backend.entity.Appointment;

public interface AsyncEmailNotificationService {
    void sendBookingConfirmation(Appointment appointment);
    void sendAppointmentReminder(Appointment appointment);
    void sendCancellationNotice(Appointment appointment);
}
