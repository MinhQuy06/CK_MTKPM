package com.datlichkhambenh.backend.repository;
import com.datlichkhambenh.backend.entity.BenefitDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface BenefitDecisionRepository extends JpaRepository<BenefitDecision,Long> {
 List<BenefitDecision> findByRequestIdOrderByIdAsc(Long requestId);
}
