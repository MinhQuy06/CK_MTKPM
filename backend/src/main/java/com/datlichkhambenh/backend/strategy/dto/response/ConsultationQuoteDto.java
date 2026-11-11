package com.datlichkhambenh.backend.strategy.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** Giá chào trả cho giao diện: loại phí, số tiền và ngày giờ của ca. */
public record ConsultationQuoteDto(Long scheduleId, String strategyKey, String strategyName,
                                   BigDecimal basePrice, BigDecimal discountRate, BigDecimal finalPrice,
                                   String currency, boolean simulated, LocalDate workDate,
                                   LocalTime startTime, LocalTime endTime) {

    /** Số JSON trong JavaScript làm mất chữ số của tỷ lệ; giao diện gửi lại chuỗi chính xác này khi xác nhận. */
    @JsonProperty("discountRateExact")
    public String discountRateExact() {
        return discountRate.toPlainString();
    }
}
