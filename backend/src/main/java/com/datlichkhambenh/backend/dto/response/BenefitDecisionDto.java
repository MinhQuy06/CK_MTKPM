package com.datlichkhambenh.backend.dto.response;
import java.time.LocalDateTime;
public record BenefitDecisionDto(Long id,String action,String reason,Long decidedBy,LocalDateTime decidedAt) {}
