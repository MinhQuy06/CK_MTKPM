package com.datlichkhambenh.backend.observer;

import com.datlichkhambenh.backend.entity.Appointment;

/** Subject state change (GoF Observer): an appointment was just marked CANCELLED. */
public record AppointmentCancelledEvent(Appointment appointment) {
}
