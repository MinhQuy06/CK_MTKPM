package com.datlichkhambenh.backend.observer;

import com.datlichkhambenh.backend.entity.Appointment;

/** Sự kiện của Subject (GoF Observer): một lịch hẹn vừa được lưu với trạng thái CONFIRMED. */
public record AppointmentBookedEvent(Appointment appointment) {
}
