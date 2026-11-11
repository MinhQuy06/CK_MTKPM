package com.datlichkhambenh.backend.repository;

import com.datlichkhambenh.backend.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

/**
 * Repository cho bảng DOCTORS.
 *
 * LƯU Ý: Cache danh sách bác sĩ / slot rảnh phải đọc từ Redis trước,
 * chỉ fallback về DB khi cache miss — xem RedisCacheService.
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Doctor d where d.userId = :id")
    Optional<Doctor> findByIdForUpdate(@Param("id") Long id);

    // Kiểm tra phone đã tồn tại chưa
    boolean existsByPhone(String phone);

    // Lấy danh sách bác sĩ theo chuyên khoa (fallback khi cache miss)
    List<Doctor> findBySpecialtyId(Long specialtyId);

    // Bác sĩ đã được Admin duyệt và tài khoản đang hoạt động
    List<Doctor> findByIsApprovedTrueAndUserIsActiveTrue();

    // Như trên, lọc thêm theo chuyên khoa
    List<Doctor> findBySpecialtyIdAndIsApprovedTrueAndUserIsActiveTrue(Long specialtyId);
}
