package com.datlichkhambenh.backend.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpecialtyDto {

    private Long id;
    private String name;
    private String description;
}
