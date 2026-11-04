package com.datlichkhambenh.backend.repository;

import com.datlichkhambenh.backend.entity.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository cho bảng SPECIALTIES.
 * Logic nghiệp vụ sẽ được bổ sung ở bước tiếp theo.
 */
@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    // Kiểm tra chuyên khoa theo tên
    boolean existsByName(String name);
}
