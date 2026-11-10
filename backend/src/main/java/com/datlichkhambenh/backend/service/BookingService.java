package com.datlichkhambenh.backend.service;

import com.datlichkhambenh.backend.dto.response.AppointmentDto;

import java.util.List;
import java.time.LocalDate;

public interface BookingService {
    /** Books a slot at the price the booking offer module confirms against {@code expectation}. */
    AppointmentDto createBooking(Long patientUserId, Long scheduleId, String symptoms,
                                 com.datlichkhambenh.backend.strategy.offer.Expectation expectation);
    void cancelBooking(Long appointmentId, Long patientUserId);
    AppointmentDto getBookingDetails(Long appointmentId, Long requesterUserId, String requesterRole);
    List<AppointmentDto> getPatientAppointments(Long patientUserId);
    List<AppointmentDto> getDoctorAppointments(Long doctorUserId);
    List<AppointmentDto> getCompletedDoctorAppointments(Long doctorUserId);
    List<AppointmentDto> getDoctorAppointments(Long doctorUserId, LocalDate from, LocalDate to);
    void updateAppointmentStatus(Long appointmentId, Long doctorUserId, String status);
}
