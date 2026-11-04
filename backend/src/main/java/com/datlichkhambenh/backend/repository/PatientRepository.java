package com.datlichkhambenh.backend.repository;

import com.datlichkhambenh.backend.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository cho bảng PATIENTS.
 * Logic nghiệp vụ sẽ được bổ sung ở bước tiếp theo.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Tìm bệnh nhân theo số điện thoại
    Optional<Patient> findByPhone(String phone);

    // Kiểm tra phone đã tồn tại chưa
    boolean existsByPhone(String phone);
}
