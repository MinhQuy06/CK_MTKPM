package com.datlichkhambenh.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class MedicalServiceRequestDto {
    @NotBlank @Size(max = 30) private String code;
    @NotBlank @Size(max = 150) private String name;
    private String description;
    private Long specialtyId;
    @NotNull @DecimalMin("0") private BigDecimal defaultPrice;
    @NotNull @Min(5) @Max(480) private Integer durationMinutes;
    private Boolean active;
}
