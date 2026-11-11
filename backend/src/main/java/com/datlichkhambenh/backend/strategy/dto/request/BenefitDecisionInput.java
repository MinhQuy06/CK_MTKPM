package com.datlichkhambenh.backend.strategy.dto.request;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
/** Quyết định của Admin; khoảng ngày hiệu lực chỉ bắt buộc khi APPROVE. */
public record BenefitDecisionInput(
        @NotBlank @Pattern(regexp = "APPROVE|REJECT") String action,
        @NotBlank(message = "Cần lý do quyết định") @Size(max = 1000) String reason,
        LocalDate approvedFrom,
        LocalDate approvedUntil) {
}
