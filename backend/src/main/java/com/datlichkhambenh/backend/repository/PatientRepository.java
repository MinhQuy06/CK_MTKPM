package com.datlichkhambenh.backend.repository;

import com.datlichkhambenh.backend.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


/**
 * Repository cho bảng PATIENTS.
 */
@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {

    // Kiểm tra phone đã tồn tại chưa
    boolean existsByPhone(String phone);
}
