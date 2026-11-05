package com.datlichkhambenh.backend.strategy.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ConsultationQuoteRequest(
        @NotNull(message = "Vui lòng chọn ca khám")
        @Positive(message = "Mã ca khám phải là số dương") Long scheduleId,
        String strategyKey) {}
