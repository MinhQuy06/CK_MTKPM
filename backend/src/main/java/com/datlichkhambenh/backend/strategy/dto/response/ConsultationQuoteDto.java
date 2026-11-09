package com.datlichkhambenh.backend.strategy.dto.response;

import java.math.BigDecimal;

public record ConsultationQuoteDto(Long scheduleId, String strategyKey, String strategyName,
                                   BigDecimal basePrice, BigDecimal discountRate, BigDecimal finalPrice,
                                   String currency, boolean simulated, java.time.LocalDate workDate,
                                   java.time.LocalTime startTime, java.time.LocalTime endTime) {
    /** JSON numbers lose configured rate precision in JavaScript; echo this string on confirmation. */
    @com.fasterxml.jackson.annotation.JsonProperty("discountRateExact")
    public String discountRateExact() { return discountRate.toPlainString(); }
}
