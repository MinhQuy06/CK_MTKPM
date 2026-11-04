package com.datlichkhambenh.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateMedicalRecordDto {

    @NotNull(message = "Mã lịch hẹn (appointmentId) không được để trống")
    private Long appointmentId;

    @NotBlank(message = "Chẩn đoán không được để trống")
    private String diagnosis;

    private String doctorNotes;
}
