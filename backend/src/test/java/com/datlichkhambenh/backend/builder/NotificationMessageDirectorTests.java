package com.datlichkhambenh.backend.builder;

import com.datlichkhambenh.backend.entity.Appointment;
import com.datlichkhambenh.backend.entity.Doctor;
import com.datlichkhambenh.backend.entity.Patient;
import com.datlichkhambenh.backend.entity.Schedule;
import com.datlichkhambenh.backend.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/** Builder ticket 05: one Director owns the recipe for every notification kind. */
class NotificationMessageDirectorTests {

    private final NotificationMessageDirector director = new NotificationMessageDirector();

    private Appointment legacyAppointmentWithoutPrice() {
        Patient patient = Patient.builder()
                .user(User.builder().email("bn@test.vn").role("PATIENT").build())
                .fullName("BN").build();
        Doctor doctor = Doctor.builder().fullName("BS").build();
        Schedule slot = Schedule.builder().doctor(doctor).workDate(LocalDate.of(2026, 11, 12))
                .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(9, 30)).build();
        Appointment appointment = new Appointment();
        appointment.setBookingCode("BK-1");
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setSchedule(slot);
        return appointment;
    }

    @Test
    void reminderHasReminderTypeAndSlotTimes() {
        var message = director.reminder(legacyAppointmentWithoutPrice());
        assertEquals("APPOINTMENT_REMINDER", message.type());
        assertEquals("BK-1", message.bookingCode());
        assertEquals("2026-11-12", message.workDate());
        assertEquals("09:30", message.endTime());
        assertNull(message.price());
    }

    @Test
    void eachKindHasItsOwnType() {
        var appointment = legacyAppointmentWithoutPrice();
        assertEquals("BOOKING_CONFIRMED", director.bookingConfirmed(appointment).type());
        assertEquals("BOOKING_CANCELLED", director.cancelled(appointment).type());
    }
}
