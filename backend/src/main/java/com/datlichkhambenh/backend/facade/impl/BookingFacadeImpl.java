package com.datlichkhambenh.backend.facade.impl;

import com.datlichkhambenh.backend.dto.request.CreateBookingDto;
import com.datlichkhambenh.backend.dto.response.AppointmentDto;
import com.datlichkhambenh.backend.entity.Appointment;
import com.datlichkhambenh.backend.entity.Doctor;
import com.datlichkhambenh.backend.entity.Patient;
import com.datlichkhambenh.backend.entity.Schedule;
import com.datlichkhambenh.backend.exception.AppException;
import com.datlichkhambenh.backend.exception.BookingConflictException;
import com.datlichkhambenh.backend.exception.ResourceNotFoundException;
import com.datlichkhambenh.backend.facade.BookingFacade;
import com.datlichkhambenh.backend.repository.AppointmentRepository;
import com.datlichkhambenh.backend.repository.PatientRepository;
import com.datlichkhambenh.backend.repository.ScheduleRepository;
import com.datlichkhambenh.backend.service.AsyncEmailNotificationService;
import com.datlichkhambenh.backend.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Hiện thực Facade Pattern cho nghiệp vụ đặt lịch khám bệnh.
 *
 * Đóng vai trò là đầu mối duy nhất điều phối hoạt động giữa các hệ thống con (Subsystems):
 * 1. ScheduleRepository: Thực hiện Pessimistic Lock trên slot khám để chống Race Condition.
 * 2. AppointmentRepository: Kiểm tra trùng lặp và lưu trữ bản ghi Appointment mới.
 * 3. PatientRepository: Xác thực thông tin bệnh nhân.
 * 4. RedisCacheService: Xóa cache danh sách slot rảnh của bác sĩ sau khi đặt.
 * 5. AsyncEmailNotificationService: Phát sự kiện gửi email xác nhận bất đồng bộ sau transaction.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BookingFacadeImpl implements BookingFacade {

    private final ScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final RedisCacheService redisCacheService;
    private final AsyncEmailNotificationService emailNotificationService;

    @Override
    @Transactional
    public AppointmentDto createBooking(Long patientUserId, CreateBookingDto request) {
        // 1. Pessimistic Write Lock trên bản ghi Schedule để tránh Race Condition khi 2 bệnh nhân đặt cùng 1 slot
        Schedule schedule = scheduleRepository.findByIdForUpdate(request.getScheduleId())
                .orElseThrow(() -> new ResourceNotFoundException("Ca khám (Schedule) không tồn tại"));

        // 2. Kiểm tra slot còn khả dụng hay không
        if (!Boolean.TRUE.equals(schedule.getIsAvailable())) {
            throw new BookingConflictException("Ca khám này đã được đặt bởi người khác hoặc không còn khả dụng");
        }
        if (appointmentRepository.existsByScheduleId(schedule.getId())) {
            throw new BookingConflictException("Ca khám này đã có lịch hẹn");
        }

        LocalDateTime slotTime = LocalDateTime.of(schedule.getWorkDate(), schedule.getStartTime());
        if (!slotTime.isAfter(LocalDateTime.now())) {
            throw new AppException("Không thể đặt ca khám đã qua");
        }

        Patient patient = patientRepository.findById(patientUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Hồ sơ bệnh nhân không tồn tại"));

        Doctor doctor = schedule.getDoctor();
        if (doctor == null) {
            throw new ResourceNotFoundException("Bác sĩ phụ trách ca khám không tồn tại");
        }
        if (!Boolean.TRUE.equals(doctor.getIsApproved()) || !Boolean.TRUE.equals(doctor.getUser().getIsActive())) {
            throw new AppException("Bác sĩ hiện không nhận lịch khám");
        }

        // 3. Tạo Appointment mới với booking_code ngẫu nhiên
        String bookingCode = "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Appointment appointment = Appointment.builder()
                .bookingCode(bookingCode)
                .patient(patient)
                .doctor(doctor)
                .schedule(schedule)
                .symptoms(request.getSymptoms())
                .status("CONFIRMED")
                .price(doctor.getConsultationFee())
                .build();

        Appointment savedAppointment = appointmentRepository.save(appointment);

        // 4. Cập nhật trạng thái slot thành không khả dụng
        schedule.setIsAvailable(false);
        scheduleRepository.save(schedule);

        // 5. Invalidate slot cache trong Redis
        redisCacheService.evictSlotCache(doctor.getUserId(), schedule.getWorkDate());

        // 6. Trigger sự kiện gửi mail bất đồng bộ (Async task, KHÔNG block response)
        try {
            emailNotificationService.sendBookingConfirmation(savedAppointment);
        } catch (Exception e) {
            log.error("Lỗi khi trigger gửi mail xác nhận đặt lịch: {}", e.getMessage());
        }

        return mapToDto(savedAppointment);
    }

    private AppointmentDto mapToDto(Appointment appointment) {
        Schedule schedule = appointment.getSchedule();
        return AppointmentDto.builder()
                .id(appointment.getId())
                .bookingCode(appointment.getBookingCode())
                .patientId(appointment.getPatient().getUserId())
                .patientName(appointment.getPatient().getFullName())
                .patientPhone(appointment.getPatient().getPhone())
                .doctorId(appointment.getDoctor().getUserId())
                .doctorName(appointment.getDoctor().getFullName())
                .scheduleId(schedule != null ? schedule.getId() : null)
                .workDate(schedule != null ? schedule.getWorkDate() : null)
                .startTime(schedule != null ? schedule.getStartTime() : null)
                .endTime(schedule != null ? schedule.getEndTime() : null)
                .symptoms(appointment.getSymptoms())
                .status(appointment.getStatus())
                .price(appointment.getPrice())
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}

