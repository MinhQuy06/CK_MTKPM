package com.datlichkhambenh.backend.repository;

import com.datlichkhambenh.backend.entity.Schedule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository cho bảng SCHEDULES.
 *
 * QUAN TRỌNG — Pessimistic Lock:
 * findByIdForUpdate() dùng SELECT ... FOR UPDATE để đảm bảo chỉ 1 transaction
 * có thể đặt lịch vào 1 slot tại 1 thời điểm.
 * BookingServiceImpl BẮT BUỘC dùng method này, không dùng findById thông thường.
 */
@Repository
public interface ScheduleRepository extends JpaRepository<Schedule, Long> {

    /**
     * Lấy slot theo id với Pessimistic Write Lock.
     * Dùng trong BookingServiceImpl để tránh race condition.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Schedule s WHERE s.id = :id")
    Optional<Schedule> findByIdForUpdate(@Param("id") Long id);

    // Lấy danh sách slot còn trống của bác sĩ theo ngày (dùng cho cache + hiển thị)
    List<Schedule> findByDoctorUserIdAndWorkDateAndIsAvailableTrue(Long doctorId, LocalDate workDate);

    // Lấy tất cả slot của bác sĩ theo ngày (cho DoctorScheduleService)
    List<Schedule> findByDoctorUserIdAndWorkDate(Long doctorId, LocalDate workDate);

    boolean existsByDoctorUserIdAndWorkDateAndStartTimeLessThanAndEndTimeGreaterThan(
            Long doctorId, LocalDate workDate, java.time.LocalTime endTime, java.time.LocalTime startTime);

    @Query("SELECT COUNT(s) > 0 FROM Schedule s WHERE s.doctor.userId = :doctorId " +
           "AND s.workDate = :workDate " +
           "AND s.startTime < :endTime AND s.endTime > :startTime " +
           "AND s.id != :excludeId")
    boolean existsDuplicateSlot(@Param("doctorId") Long doctorId, 
                                 @Param("workDate") LocalDate workDate, 
                                 @Param("startTime") java.time.LocalTime startTime, 
                                 @Param("endTime") java.time.LocalTime endTime, 
                                 @Param("excludeId") Long excludeId);

    boolean existsByDoctorUserId(Long doctorId);

    List<Schedule> findByDoctorUserIdAndWorkDateGreaterThanEqualOrderByWorkDateAscStartTimeAsc(
            Long doctorId, LocalDate workDate);
}
