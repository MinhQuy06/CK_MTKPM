package com.datlichkhambenh.backend.dto.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduleDto {

    private Long id;
    private Long doctorId;
    private String doctorName;
    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private Boolean isAvailable;
    private Boolean isBooked;
}
