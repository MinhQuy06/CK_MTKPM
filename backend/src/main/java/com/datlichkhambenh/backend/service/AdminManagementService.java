package com.datlichkhambenh.backend.service;

import com.datlichkhambenh.backend.dto.request.SpecialtyRequestDto;
import com.datlichkhambenh.backend.dto.response.AppointmentDto;
import com.datlichkhambenh.backend.dto.response.DoctorDto;
import com.datlichkhambenh.backend.dto.response.SpecialtyDto;
import com.datlichkhambenh.backend.dto.response.UserDto;
import com.datlichkhambenh.backend.dto.response.PatientDto;
import com.datlichkhambenh.backend.dto.request.AdminCreateDoctorDto;
import com.datlichkhambenh.backend.dto.request.AdminCreatePatientDto;
import com.datlichkhambenh.backend.dto.request.UpdateDoctorProfileDto;
import com.datlichkhambenh.backend.dto.request.UpdatePatientProfileDto;

import java.util.List;
import java.util.Map;

public interface AdminManagementService {
    void approveDoctor(Long doctorUserId);
    void rejectDoctor(Long doctorUserId);
    SpecialtyDto createSpecialty(SpecialtyRequestDto request);
    List<SpecialtyDto> getAllSpecialties();
    List<DoctorDto> getPendingDoctors();
    Map<String, Object> getSystemStats();
    List<UserDto> getUsers(String role);
    void setUserActive(Long userId, boolean active);
    SpecialtyDto updateSpecialty(Long id, SpecialtyRequestDto request);
    void deleteSpecialty(Long id);
    Map<String, Object> getAnalytics(int year);
    List<DoctorDto> getDoctors();
    DoctorDto createDoctor(AdminCreateDoctorDto request);
    DoctorDto updateDoctor(Long doctorId, UpdateDoctorProfileDto request);
    void deleteDoctor(Long doctorId);
    List<PatientDto> getPatients();
    PatientDto createPatient(AdminCreatePatientDto request);
    PatientDto updatePatient(Long patientId, UpdatePatientProfileDto request);
    void deletePatient(Long patientId);
    List<AppointmentDto> getAppointments(int page, int size);
    List<AppointmentDto> getPatientAppointments(Long patientId);
}
