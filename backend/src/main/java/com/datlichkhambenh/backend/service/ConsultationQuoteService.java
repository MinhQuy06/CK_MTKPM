package com.datlichkhambenh.backend.service;

import com.datlichkhambenh.backend.dto.response.ConsultationQuoteDto;
import com.datlichkhambenh.backend.entity.Doctor;
import com.datlichkhambenh.backend.entity.Schedule;
import com.datlichkhambenh.backend.exception.BookingConflictException;
import com.datlichkhambenh.backend.exception.ResourceNotFoundException;
import com.datlichkhambenh.backend.repository.AppointmentRepository;
import com.datlichkhambenh.backend.repository.ScheduleRepository;
import com.datlichkhambenh.backend.service.pricing.PricingAmounts;
import com.datlichkhambenh.backend.service.pricing.PricingContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ConsultationQuoteService {
    private final ScheduleRepository schedules;
    private final AppointmentRepository appointments;
    private final PricingContext pricing;

    public ConsultationQuoteService(ScheduleRepository schedules, AppointmentRepository appointments,
                                    PricingContext pricing) {
        this.schedules = schedules;
        this.appointments = appointments;
        this.pricing = pricing;
    }

    @Transactional(readOnly = true)
    public ConsultationQuoteDto quote(Long scheduleId, String strategyKey) {
        Schedule schedule = schedules.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Ca khám không tồn tại"));
        if (!Boolean.TRUE.equals(schedule.getIsAvailable()) || appointments.existsByScheduleId(scheduleId)) {
            throw new BookingConflictException("Ca khám không còn khả dụng. Vui lòng chọn ca khác.");
        }
        if (!LocalDateTime.of(schedule.getWorkDate(), schedule.getStartTime()).isAfter(LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")))) {
            throw new BookingConflictException("Không thể báo giá ca khám đã qua");
        }
        Doctor doctor = schedule.getDoctor();
        if (doctor == null || doctor.getUser() == null || !Boolean.TRUE.equals(doctor.getIsApproved())
                || !Boolean.TRUE.equals(doctor.getUser().getIsActive())) {
            throw new BookingConflictException("Bác sĩ hiện không nhận lịch khám");
        }
        PricingAmounts amount = pricing.calculate(strategyKey, doctor.getConsultationFee());
        return new ConsultationQuoteDto(scheduleId, amount.strategyKey(), amount.strategyName(), amount.basePrice(),
                amount.discountRate(), amount.finalPrice(), "VND", amount.simulated());
    }
}
