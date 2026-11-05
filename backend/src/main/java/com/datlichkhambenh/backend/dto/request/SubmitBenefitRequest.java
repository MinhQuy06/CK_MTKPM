package com.datlichkhambenh.backend.dto.request;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record SubmitBenefitRequest(
        @NotBlank @Pattern(regexp="INSURANCE|VIP",message="Loại quyền phải là INSURANCE hoặc VIP") String kind,
        @NotBlank(message="Mã hồ sơ không được để trống") @Size(max=120) String referenceCode,
        @NotNull(message="Cần ngày bắt đầu") LocalDate requestedFrom,
        @NotNull(message="Cần ngày kết thúc") LocalDate requestedUntil, Long renewalOfId) {}
