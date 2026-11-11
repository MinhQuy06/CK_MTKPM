package com.datlichkhambenh.backend.strategy.repository;

import com.datlichkhambenh.backend.strategy.entity.BenefitRequest;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BenefitRequestRepository extends JpaRepository<BenefitRequest, Long> {

    /** Khóa ghi một yêu cầu đến hết giao dịch (đặt lịch và thu hồi tuần tự hóa qua khóa này). */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from BenefitRequest r where r.id = :id")
    Optional<BenefitRequest> findForUpdate(@Param("id") Long id);

    /** Chỉ lấy ID trước khi khóa, để không giữ bản entity cũ trong giao dịch. */
    @Query("select r.id from BenefitRequest r where r.patient.userId = :patientId and r.kind = :kind"
            + " and r.status = 'APPROVED' order by r.id desc")
    List<Long> findApprovedIds(@Param("patientId") Long patientId, @Param("kind") String kind);

    List<BenefitRequest> findAllByOrderByCreatedAtDescIdDesc();

    List<BenefitRequest> findByPatientUserIdAndKindAndStatusOrderByIdDesc(Long patientId, String kind, String status);

    List<BenefitRequest> findByPatientUserIdOrderByCreatedAtDescIdDesc(Long patientId);

    Optional<BenefitRequest> findByIdAndPatientUserId(Long id, Long patientId);
}
