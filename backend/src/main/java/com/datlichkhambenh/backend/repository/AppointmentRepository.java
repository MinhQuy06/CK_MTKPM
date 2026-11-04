package com.datlichkhambenh.backend.repository;

import com.datlichkhambenh.backend.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

/**
 * Repository cho bảng APPOINTMENTS.
 * Logic nghiệp vụ sẽ được bổ sung ở bước tiếp theo.
 */
@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Appointment a where a.id = :id")
    Optional<Appointment> findByIdForUpdate(@Param("id") Long id);

    // Tìm lịch hẹn theo booking_code (mã đặt chỗ duy nhất)
    Optional<Appointment> findByBookingCode(String bookingCode);

    // Lịch sử đặt khám của bệnh nhân (xem lịch sử / hủy lịch)
    List<Appointment> findByPatientUserId(Long patientId);

    // Danh sách bệnh nhân đã khám của bác sĩ
    List<Appointment> findByDoctorUserId(Long doctorId);

    // Kiểm tra slot đã bị đặt chưa (trước khi lock)
    boolean existsByScheduleIdAndStatusNot(Long scheduleId, String status);

    boolean existsByScheduleId(Long scheduleId);

    boolean existsByPatientUserId(Long patientId);

    boolean existsByDoctorUserId(Long doctorId);

    List<Appointment> findByDoctorUserIdAndStatusOrderByScheduleWorkDateDesc(Long doctorId, String status);

    List<Appointment> findByDoctorUserIdAndScheduleWorkDateBetween(Long doctorId, LocalDate from, LocalDate to);

    @Query("select a.doctor.fullName, count(a) from Appointment a where a.status <> 'CANCELLED' group by a.doctor.userId, a.doctor.fullName order by count(a) desc")
    List<Object[]> findTopDoctors();

    long countByStatus(String status);
}
