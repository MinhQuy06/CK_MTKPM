package com.datlichkhambenh.backend.repository;
import com.datlichkhambenh.backend.entity.BenefitRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BenefitRequestRepository extends JpaRepository<BenefitRequest,Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select r from BenefitRequest r where r.id = :id")
    Optional<BenefitRequest> findForUpdate(@org.springframework.data.repository.query.Param("id") Long id);
    @org.springframework.data.jpa.repository.Query("select r.id from BenefitRequest r where r.patient.userId=:patientId and r.kind=:kind and r.status='APPROVED' order by r.id desc")
    List<Long> findApprovedIds(@org.springframework.data.repository.query.Param("patientId") Long patientId,@org.springframework.data.repository.query.Param("kind") String kind);
    List<BenefitRequest> findAllByOrderByCreatedAtDescIdDesc();
    List<BenefitRequest> findByPatientUserIdAndKindAndStatusOrderByIdDesc(Long patientId,String kind,String status);
    List<BenefitRequest> findByPatientUserIdOrderByCreatedAtDescIdDesc(Long patientId);
    Optional<BenefitRequest> findByIdAndPatientUserId(Long id,Long patientId);
}
