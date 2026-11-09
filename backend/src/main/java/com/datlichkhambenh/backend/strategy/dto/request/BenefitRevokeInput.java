package com.datlichkhambenh.backend.strategy.dto.request;

import jakarta.validation.constraints.*;
public record BenefitRevokeInput(@NotBlank(message="Cần lý do thu hồi") @Size(max=1000) String reason) {}
