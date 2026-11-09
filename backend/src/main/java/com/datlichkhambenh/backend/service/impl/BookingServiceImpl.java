package com.datlichkhambenh.backend.service.impl;

import com.datlichkhambenh.backend.dto.request.CreateBookingDto;
import com.datlichkhambenh.backend.dto.response.AppointmentDto;
import com.datlichkhambenh.backend.entity.Appointment;
import com.datlichkhambenh.backend.entity.Doctor;
import com.datlichkhambenh.backend.entity.Patient;
import com.datlichkhambenh.backend.entity.Schedule;
import com.datlichkhambenh.backend.exception.AppException;
import com.datlichkhambenh.backend.exception.ResourceNotFoundException;
import com.datlichkhambenh.backend.facade.BookingFacade;
import com.datlichkhambenh.backend.repository.AppointmentRepository;
import com.datlichkhambenh.backend.repository.DoctorRepository;
import com.datlichkhambenh.backend.repository.PatientRepository;
import com.datlichkhambenh.backend.repository.ScheduleRepository;
import com.datlichkhambenh.backend.service.AsyncEmailNotificationService;
import com.datlichkhambenh.backend.service.BookingService;
import com.datlichkhambenh.backend.service.RedisCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingFacade bookingFacade;
    private final ScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AsyncEmailNotificationService emailNotificationService;
    private final RedisCacheService redisCacheService;

    @Override
    @Transactional
    public AppointmentDto createBooking(Long patientUserId, CreateBookingDto request) {
        return bookingFacade.createBooking(patientUserId, request);
    }

    @Override
    @Transactional
    public void cancelBooking(Long appointmentId, Long patientUserId) {
        Appointment appointment = appointmentRepository.findByIdForUpdate(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Lịch hẹn không tồn tại"));

        if (!appointment.getPatient().getUserId().equals(patientUserId)) {
            throw new AppException("Bạn không có quyền hủy lịch hẹn này");
        }

        if ("CANCELLED".equals(appointment.getStatus())) {
            throw new AppException("Lịch hẹn này đã bị hủy trước đó");
        }
        if ("COMPLETED".equals(appointment.getStatus())) {
            throw new AppException("Không thể hủy lịch khám đã hoàn thành");
        }
        Schedule bookedSchedule = appointment.getSchedule();
        LocalDateTime appointmentTime = LocalDateTime.of(bookedSchedule.getWorkDate(), bookedSchedule.getStartTime());
        if (!appointmentTime.isAfter(LocalDateTime.now().plusHours(24))) {
            throw new AppException("Chỉ có thể hủy lịch trước giờ khám ít nhất 24 giờ");
        }

        appointment.setStatus("CANCELLED");
        appointmentRepository.save(appointment);

        // Quan hệ Appointment-Schedule là 1-1. Tạo slot thay thế để giữ lịch sử
        // lịch đã hủy mà vẫn cho phép bệnh nhân khác đặt cùng khung giờ.
        Schedule schedule = bookedSchedule;
        if (schedule != null) {
            schedule.setIsAvailable(false);
            scheduleRepository.save(schedule);
            scheduleRepository.save(Schedule.builder()
                    .doctor(schedule.getDoctor())
                    .workDate(schedule.getWorkDate())
                    .startTime(schedule.getStartTime())
                    .endTime(schedule.getEndTime())
                    .isAvailable(true)
                    .build());
            redisCacheService.evictSlotCache(schedule.getDoctor().getUserId(), schedule.getWorkDate());
        }

        // Trigger mail hủy lịch bất đồng bộ
        try {
            emailNotificationService.sendCancellationNotice(appointment);
        } catch (Exception e) {
            log.error("Lỗi khi trigger mail hủy lịch: {}", e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentDto getBookingDetails(Long appointmentId, Long requesterUserId, String requesterRole) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Lịch hẹn không tồn tại"));
        boolean ownsAppointment = "ADMIN".equals(requesterRole)
                || ("PATIENT".equals(requesterRole) && appointment.getPatient().getUserId().equals(requesterUserId))
                || ("DOCTOR".equals(requesterRole) && appointment.getDoctor().getUserId().equals(requesterUserId));
        if (!ownsAppointment) {
            throw new AppException("Bạn không có quyền xem lịch hẹn này", org.springframework.http.HttpStatus.FORBIDDEN);
        }
        return mapToDto(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getPatientAppointments(Long patientUserId) {
        List<AppointmentDto> list = appointmentRepository.findByPatientUserId(patientUserId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        // Sắp xếp lịch hẹn theo ngày giảm dần (mới nhất lên đầu)
        list.sort((a, b) -> {
            if (a.getWorkDate() == null || b.getWorkDate() == null) return 0;
            return b.getWorkDate().compareTo(a.getWorkDate());
        });

        return list;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getDoctorAppointments(Long doctorUserId) {
        return appointmentRepository.findByDoctorUserId(doctorUserId).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getCompletedDoctorAppointments(Long doctorUserId) {
        return appointmentRepository
                .findByDoctorUserIdAndStatusOrderByScheduleWorkDateDesc(doctorUserId, "COMPLETED")
                .stream().map(this::mapToDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<AppointmentDto> getDoctorAppointments(Long doctorUserId, java.time.LocalDate from,
                                                       java.time.LocalDate to) {
        if (from.isAfter(to)) {
            throw new AppException("Ngày bắt đầu phải trước hoặc bằng ngày kết thúc");
        }
        return appointmentRepository.findByDoctorUserIdAndScheduleWorkDateBetween(doctorUserId, from, to)
                .stream().map(this::mapToDto).toList();
    }

    @Override @Transactional
    public void updateAppointmentStatus(Long appointmentId, Long doctorUserId, String status) {
        Appointment appointment = appointmentRepository.findByIdForUpdate(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Lịch hẹn không tồn tại"));
        if (!appointment.getDoctor().getUserId().equals(doctorUserId)) throw new AppException("Bạn không quản lý lịch hẹn này");
        if (!"COMPLETED".equals(status)) throw new AppException("Bác sĩ chỉ được chuyển lịch sang COMPLETED");
        if (!"CONFIRMED".equals(appointment.getStatus())) throw new AppException("Chỉ có thể hoàn thành lịch đang xác nhận");
        LocalDateTime appointmentTime = LocalDateTime.of(
                appointment.getSchedule().getWorkDate(), appointment.getSchedule().getStartTime());
        if (appointmentTime.isAfter(LocalDateTime.now())) throw new AppException("Chưa đến giờ khám");
        appointment.setStatus(status); appointmentRepository.save(appointment);
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
