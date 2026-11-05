package com.datlichkhambenh.backend.dto.response;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
public record BenefitRequestDto(Long id,Long patientId,String patientName,String kind,String referenceCode,
 LocalDate requestedFrom,LocalDate requestedUntil,String status,LocalDateTime createdAt,
 LocalDate approvedFrom,LocalDate approvedUntil,List<BenefitDecisionDto> decisions) {}
