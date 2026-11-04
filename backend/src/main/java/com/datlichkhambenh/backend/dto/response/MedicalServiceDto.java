package com.datlichkhambenh.backend.dto.response;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class MedicalServiceDto {
    private Long id; private String code; private String name; private String description;
    private Long specialtyId; private String specialtyName; private BigDecimal defaultPrice;
    private Integer durationMinutes; private Boolean active;
}
