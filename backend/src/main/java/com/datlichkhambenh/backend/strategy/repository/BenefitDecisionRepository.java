package com.datlichkhambenh.backend.strategy.repository;

import com.datlichkhambenh.backend.strategy.entity.BenefitDecision;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BenefitDecisionRepository extends JpaRepository<BenefitDecision, Long> {

    /** Lịch sử quyết định của một yêu cầu, cũ nhất trước. */
    List<BenefitDecision> findByRequestIdOrderByIdAsc(Long requestId);
}
