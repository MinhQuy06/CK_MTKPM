package com.datlichkhambenh.backend.strategy.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
/** Yêu cầu quyền hưởng trả cho giao diện; effectiveStatus tính thêm EXPIRED / NOT_YET_VALID theo ngày hôm nay. */
public record BenefitRequestDto(Long id, Long patientId, String patientName, String kind, String referenceCode,
                                LocalDate requestedFrom, LocalDate requestedUntil, String status,
                                LocalDateTime createdAt, LocalDate approvedFrom, LocalDate approvedUntil,
                                Long renewalOfId, String effectiveStatus, List<BenefitDecisionDto> decisions) {
}
