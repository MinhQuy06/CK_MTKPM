package com.datlichkhambenh.backend.dto.response;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BenefitRequestDto(Long id,String kind,String referenceCode,LocalDate requestedFrom,
        LocalDate requestedUntil,String status,LocalDateTime createdAt) {}
