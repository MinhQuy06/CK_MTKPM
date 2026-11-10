package com.datlichkhambenh.backend.observer;

import com.datlichkhambenh.backend.entity.Appointment;

/** Subject state change (GoF Observer): an appointment was just saved as CONFIRMED. */
public record AppointmentBookedEvent(Appointment appointment) {
}
