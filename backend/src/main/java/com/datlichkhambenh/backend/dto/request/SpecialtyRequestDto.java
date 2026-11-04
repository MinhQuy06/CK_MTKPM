package com.datlichkhambenh.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecialtyRequestDto {

    @NotBlank(message = "Tên chuyên khoa không được để trống")
    private String name;

    private String description;
}
