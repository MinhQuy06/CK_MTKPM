package com.datlichkhambenh.backend.strategy.service;

import com.datlichkhambenh.backend.strategy.pricing.PricingAmounts;
import com.datlichkhambenh.backend.strategy.pricing.PricingChoice;
import com.datlichkhambenh.backend.strategy.pricing.PricingContext;

import com.datlichkhambenh.backend.strategy.dto.response.ConsultationQuoteDto;
import com.datlichkhambenh.backend.entity.*;
import com.datlichkhambenh.backend.exception.*;
import com.datlichkhambenh.backend.repository.*;
import com.datlichkhambenh.backend.strategy.pricing.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.List;

@Service
public class ConsultationQuoteService {
 private final ScheduleRepository schedules; private final AppointmentRepository appointments;
 private final PricingContext pricing; private final BenefitService benefits;
 public ConsultationQuoteService(ScheduleRepository schedules,AppointmentRepository appointments,PricingContext pricing,BenefitService benefits) {
  this.schedules=schedules;this.appointments=appointments;this.pricing=pricing;this.benefits=benefits;
 }
 @Transactional(readOnly=true)
 public ConsultationQuoteDto quote(Long patientId,Long scheduleId,String strategyKey) {
  Schedule schedule=bookableSchedule(scheduleId);
  String key=strategyKey==null?"STANDARD":strategyKey;
  if(!pricing.supports(key))throw new IllegalArgumentException("Loại tính phí không được hỗ trợ");
  if(!"STANDARD".equals(key)&&benefits.eligible(patientId,key,schedule.getWorkDate()).isEmpty())
   throw new BookingRejectionException("BENEFIT_UNAVAILABLE","Bạn không đủ điều kiện hưởng loại phí này cho ngày khám đã chọn",HttpStatus.FORBIDDEN);
  PricingAmounts amount=pricing.calculate(key,schedule.getDoctor().getConsultationFee());
  return new ConsultationQuoteDto(scheduleId,amount.strategyKey(),amount.strategyName(),amount.basePrice(),amount.discountRate(),amount.finalPrice(),"VND",false,schedule.getWorkDate(),schedule.getStartTime(),schedule.getEndTime());
 }
 @Transactional(readOnly=true)
 public List<PricingChoice> options(Long patientId,Long scheduleId) {
  Schedule schedule=bookableSchedule(scheduleId);
  return pricing.choices().stream().filter(c->"STANDARD".equals(c.key())||benefits.eligible(patientId,c.key(),schedule.getWorkDate()).isPresent()).toList();
 }
 private Schedule bookableSchedule(Long id) {
  var schedule=schedules.findById(id).orElseThrow(()->new BookingRejectionException("SLOT_UNAVAILABLE","Ca khám không tồn tại. Vui lòng chọn lại ca.",HttpStatus.NOT_FOUND));
  if(!Boolean.TRUE.equals(schedule.getIsAvailable())||appointments.existsByScheduleId(id))
   throw new BookingRejectionException("SLOT_UNAVAILABLE","Ca khám không còn khả dụng. Vui lòng chọn ca khác.",HttpStatus.CONFLICT);
  if(!LocalDateTime.of(schedule.getWorkDate(),schedule.getStartTime()).isAfter(LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))))
   throw new BookingRejectionException("SLOT_UNAVAILABLE","Không thể báo giá ca khám đã qua",HttpStatus.CONFLICT);
  Doctor doctor=schedule.getDoctor();
  if(doctor==null||doctor.getUser()==null||!Boolean.TRUE.equals(doctor.getIsApproved())||!Boolean.TRUE.equals(doctor.getUser().getIsActive()))
   throw new BookingConflictException("Bác sĩ hiện không nhận lịch khám");
  return schedule;
 }
}
