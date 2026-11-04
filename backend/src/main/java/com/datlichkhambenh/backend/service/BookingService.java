package com.datlichkhambenh.backend.service;

import com.datlichkhambenh.backend.dto.request.CreateBookingDto;
import com.datlichkhambenh.backend.dto.response.AppointmentDto;

import java.util.List;
import java.time.LocalDate;

public interface BookingService {
    AppointmentDto createBooking(Long patientUserId, CreateBookingDto request);
    void cancelBooking(Long appointmentId, Long patientUserId);
    AppointmentDto getBookingDetails(Long appointmentId, Long requesterUserId, String requesterRole);
    List<AppointmentDto> getPatientAppointments(Long patientUserId);
    List<AppointmentDto> getDoctorAppointments(Long doctorUserId);
    List<AppointmentDto> getCompletedDoctorAppointments(Long doctorUserId);
    List<AppointmentDto> getDoctorAppointments(Long doctorUserId, LocalDate from, LocalDate to);
    void updateAppointmentStatus(Long appointmentId, Long doctorUserId, String status);
}
