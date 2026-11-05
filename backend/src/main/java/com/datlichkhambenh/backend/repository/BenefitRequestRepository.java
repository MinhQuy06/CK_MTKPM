package com.datlichkhambenh.backend.repository;
import com.datlichkhambenh.backend.entity.BenefitRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface BenefitRequestRepository extends JpaRepository<BenefitRequest,Long> {
    List<BenefitRequest> findByPatientUserIdOrderByCreatedAtDescIdDesc(Long patientId);
    Optional<BenefitRequest> findByIdAndPatientUserId(Long id,Long patientId);
}
