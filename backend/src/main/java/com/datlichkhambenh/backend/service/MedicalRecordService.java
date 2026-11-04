package com.datlichkhambenh.backend.service;

import com.datlichkhambenh.backend.dto.request.UpdateMedicalRecordDto;

/**
 * MedicalRecordService — Nghiệp vụ cập nhật hồ sơ bệnh án sau khám.
 * Controller chỉ được gọi interface này, không được trực tiếp dùng Repository.
 */
public interface MedicalRecordService {

    /**
     * Bác sĩ ghi chẩn đoán và notes sau khi khám xong.
     * Đồng thời chuyển trạng thái Appointment sang COMPLETED.
     *
     * @param request DTO chứa appointmentId, diagnosis, doctorNotes
     */
    void updateMedicalRecord(Long doctorUserId, UpdateMedicalRecordDto request);
}
