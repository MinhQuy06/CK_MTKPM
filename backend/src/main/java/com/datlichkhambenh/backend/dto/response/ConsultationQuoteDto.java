package com.datlichkhambenh.backend.dto.response;

import java.math.BigDecimal;

public record ConsultationQuoteDto(Long scheduleId, String strategyKey, String strategyName,
                                   BigDecimal basePrice, BigDecimal discountRate, BigDecimal finalPrice,
                                   String currency, boolean simulated) {}
