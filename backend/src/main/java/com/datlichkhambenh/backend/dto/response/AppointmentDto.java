package com.datlichkhambenh.backend.dto.response;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentDto {

    private Long id;
    private String bookingCode;
    private Long patientId;
    private String patientName;
    private String patientPhone;
    private Long doctorId;
    private String doctorName;
    private Long scheduleId;
    private LocalDate workDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private String symptoms;
    private String status;
    private BigDecimal price;
    private String pricingKey;
    private Long benefitRequestId;
    private BigDecimal basePrice;
    private BigDecimal discountRate;
    private LocalDateTime createdAt;
    /** Chẩn đoán và lời dặn từ bệnh án (null nếu chưa có bệnh án). */
    private String diagnosis;
    private String doctorNotes;
}
