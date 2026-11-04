package com.datlichkhambenh.backend.repository;

import com.datlichkhambenh.backend.entity.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho bảng MEDICAL_RECORDS.
 * Logic nghiệp vụ sẽ được bổ sung ở bước tiếp theo.
 */
@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, Long> {

    // Lấy hồ sơ khám theo appointment (quan hệ 1-1 — luôn trả về tối đa 1 bản ghi)
    Optional<MedicalRecord> findByAppointmentId(Long appointmentId);

    // Lấy danh sách hồ sơ khám của 1 bệnh nhân
    List<MedicalRecord> findByAppointmentPatientUserId(Long patientUserId);
}
