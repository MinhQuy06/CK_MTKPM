package com.datlichkhambenh.backend.service.impl;

import com.datlichkhambenh.backend.dto.request.UpdateMedicalRecordDto;
import com.datlichkhambenh.backend.entity.Appointment;
import com.datlichkhambenh.backend.entity.MedicalRecord;
import com.datlichkhambenh.backend.exception.ResourceNotFoundException;
import com.datlichkhambenh.backend.exception.AppException;
import com.datlichkhambenh.backend.repository.AppointmentRepository;
import com.datlichkhambenh.backend.repository.MedicalRecordRepository;
import com.datlichkhambenh.backend.service.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {

    private final AppointmentRepository appointmentRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    @Override
    @Transactional
    public void updateMedicalRecord(Long doctorUserId, UpdateMedicalRecordDto request) {
        // 1. Tìm lịch hẹn — ném 404 nếu không tồn tại
        Appointment appointment = appointmentRepository.findByIdForUpdate(request.getAppointmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Lịch hẹn không tồn tại"));

        if (!appointment.getDoctor().getUserId().equals(doctorUserId)) {
            throw new AppException("Bạn không quản lý lịch hẹn này");
        }
        if (!"CONFIRMED".equals(appointment.getStatus()) && !"COMPLETED".equals(appointment.getStatus())) {
            throw new AppException("Không thể ghi bệnh án cho lịch đã hủy");
        }
        java.time.LocalDateTime appointmentTime = java.time.LocalDateTime.of(
                appointment.getSchedule().getWorkDate(), appointment.getSchedule().getStartTime());
        if (appointmentTime.isAfter(java.time.LocalDateTime.now())) {
            throw new AppException("Chỉ có thể ghi bệnh án khi đã đến giờ khám");
        }

        // 2. Tạo mới hoặc lấy bệnh án hiện có (1-1 với Appointment)
        MedicalRecord record = medicalRecordRepository.findByAppointmentId(appointment.getId())
                .orElseGet(() -> MedicalRecord.builder().appointment(appointment).build());

        // 3. Cập nhật nội dung chẩn đoán
        record.setDiagnosis(request.getDiagnosis());
        record.setDoctorNotes(request.getDoctorNotes());
        medicalRecordRepository.save(record);

        // 4. Chuyển trạng thái lịch hẹn sang COMPLETED
        appointment.setStatus("COMPLETED");
        appointmentRepository.save(appointment);
    }
}
