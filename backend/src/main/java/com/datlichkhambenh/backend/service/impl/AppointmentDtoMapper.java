package com.datlichkhambenh.backend.service.impl;

import com.datlichkhambenh.backend.dto.response.AppointmentDto;
import com.datlichkhambenh.backend.entity.Appointment;
import com.datlichkhambenh.backend.entity.MedicalRecord;
import com.datlichkhambenh.backend.entity.Schedule;
import com.datlichkhambenh.backend.repository.MedicalRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Chuyển lịch hẹn sang {@link AppointmentDto}, kèm chẩn đoán/lời dặn từ bệnh án.
 * Dùng chung cho bệnh nhân, bác sĩ và Admin; danh sách được nạp bệnh án theo lô (một truy vấn).
 */
@Component
@RequiredArgsConstructor
public class AppointmentDtoMapper {
    private final MedicalRecordRepository medicalRecords;

    public AppointmentDto toDto(Appointment appointment) {
        MedicalRecord record = appointment.getId() == null ? null
                : medicalRecords.findByAppointmentId(appointment.getId()).orElse(null);
        return map(appointment, record);
    }

    public List<AppointmentDto> toDtos(List<Appointment> appointments) {
        List<Long> ids = appointments.stream().map(Appointment::getId).toList();
        Map<Long, MedicalRecord> records = ids.isEmpty() ? Map.of()
                : medicalRecords.findByAppointmentIdIn(ids).stream()
                .collect(Collectors.toMap(r -> r.getAppointment().getId(), Function.identity(), (a, b) -> a));
        return appointments.stream().map(a -> map(a, records.get(a.getId()))).collect(Collectors.toList());
    }

    private static AppointmentDto map(Appointment a, MedicalRecord record) {
        Schedule s = a.getSchedule();
        return AppointmentDto.builder()
                .id(a.getId())
                .bookingCode(a.getBookingCode())
                .patientId(a.getPatient() != null ? a.getPatient().getUserId() : null)
                .patientName(a.getPatient() != null ? a.getPatient().getFullName() : null)
                .patientPhone(a.getPatient() != null ? a.getPatient().getPhone() : null)
                .doctorId(a.getDoctor() != null ? a.getDoctor().getUserId() : null)
                .doctorName(a.getDoctor() != null ? a.getDoctor().getFullName() : null)
                .scheduleId(s != null ? s.getId() : null)
                .workDate(s != null ? s.getWorkDate() : null)
                .startTime(s != null ? s.getStartTime() : null)
                .endTime(s != null ? s.getEndTime() : null)
                .symptoms(a.getSymptoms())
                .status(a.getStatus())
                .price(a.getPrice())
                .pricingKey(a.getPricingKey())
                .basePrice(a.getBasePrice())
                .discountRate(a.getDiscountRate())
                .benefitRequestId(a.getBenefitRequestId())
                .createdAt(a.getCreatedAt())
                .diagnosis(record != null ? record.getDiagnosis() : null)
                .doctorNotes(record != null ? record.getDoctorNotes() : null)
                .build();
    }
}
