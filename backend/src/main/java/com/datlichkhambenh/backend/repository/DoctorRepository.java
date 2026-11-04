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
 * Logic nghiệp vụ sẽ được bổ sung ở bước tiếp theo.
 *
 * LƯU Ý: Cache danh sách bác sĩ / slot rảnh phải đọc từ Redis trước,
 * chỉ fallback về DB khi cache miss — xem RedisCacheService.
 */
@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Doctor d where d.userId = :id")
    Optional<Doctor> findByIdForUpdate(@Param("id") Long id);

    // Tìm bác sĩ theo số điện thoại
    Optional<Doctor> findByPhone(String phone);

    // Kiểm tra phone đã tồn tại chưa
    boolean existsByPhone(String phone);

    // Lấy danh sách bác sĩ theo chuyên khoa (fallback khi cache miss)
    List<Doctor> findBySpecialtyId(Long specialtyId);

    // Lấy danh sách bác sĩ đã được Admin phê duyệt
    List<Doctor> findByIsApprovedTrue();

    List<Doctor> findByIsApprovedTrueAndUserIsActiveTrue();

    // Lấy danh sách bác sĩ theo chuyên khoa VÀ đã được duyệt
    List<Doctor> findBySpecialtyIdAndIsApprovedTrue(Long specialtyId);

    List<Doctor> findBySpecialtyIdAndIsApprovedTrueAndUserIsActiveTrue(Long specialtyId);
}
