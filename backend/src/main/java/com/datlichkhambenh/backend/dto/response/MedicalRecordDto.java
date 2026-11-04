package com.datlichkhambenh.backend.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecordDto {

    private Long id;
    private Long appointmentId;
    private String bookingCode;
    private String patientName;
    private String doctorName;
    private String diagnosis;
    private String doctorNotes;
    private LocalDateTime createdAt;
}
