package com.datlichkhambenh.backend.facade;

import com.datlichkhambenh.backend.dto.request.CreateBookingDto;
import com.datlichkhambenh.backend.dto.response.AppointmentDto;
import com.datlichkhambenh.backend.entity.Appointment;
import com.datlichkhambenh.backend.entity.Doctor;
import com.datlichkhambenh.backend.entity.Patient;
import com.datlichkhambenh.backend.entity.Schedule;
import com.datlichkhambenh.backend.entity.User;
import com.datlichkhambenh.backend.exception.AppException;
import com.datlichkhambenh.backend.exception.BookingConflictException;
import com.datlichkhambenh.backend.exception.ResourceNotFoundException;
import com.datlichkhambenh.backend.facade.impl.BookingFacadeImpl;
import com.datlichkhambenh.backend.repository.AppointmentRepository;
import com.datlichkhambenh.backend.repository.PatientRepository;
import com.datlichkhambenh.backend.repository.ScheduleRepository;
import com.datlichkhambenh.backend.service.AsyncEmailNotificationService;
import com.datlichkhambenh.backend.service.RedisCacheService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingFacadeImplTests {

    @Mock
    private ScheduleRepository scheduleRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private RedisCacheService redisCacheService;

    @Mock
    private AsyncEmailNotificationService emailNotificationService;

    @InjectMocks
    private BookingFacadeImpl bookingFacade;

    private Patient patient;
    private Doctor doctor;
    private Schedule schedule;

    @BeforeEach
    void setUp() {
        User patientUser = User.builder().id(10L).email("patient@test.com").role("PATIENT").isActive(true).build();
        patient = Patient.builder().userId(10L).user(patientUser).fullName("Nguyen Van Benh Nhan").phone("0901112233").build();

        User docUser = User.builder().id(20L).email("doctor@test.com").role("DOCTOR").isActive(true).build();
        doctor = Doctor.builder()
                .userId(20L)
                .user(docUser)
                .fullName("BS. Tran Van Bac Si")
                .consultationFee(BigDecimal.valueOf(300000))
                .isApproved(true)
                .build();

        schedule = Schedule.builder()
                .id(100L)
                .doctor(doctor)
                .workDate(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(9, 30))
                .isAvailable(true)
                .build();
    }

    @Test
    @DisplayName("Facade điều phối thành công toàn bộ quy trình đặt lịch: khóa slot, lưu appointment, xóa cache, gửi mail")
    void successfulBookingCoordination() {
        CreateBookingDto request = new CreateBookingDto(100L, "Dau dau chong mat");

        when(scheduleRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(schedule));
        when(appointmentRepository.existsByScheduleId(100L)).thenReturn(false);
        when(patientRepository.findById(10L)).thenReturn(Optional.of(patient));
        when(appointmentRepository.save(any(Appointment.class))).thenAnswer(invocation -> {
            Appointment app = invocation.getArgument(0);
            app.setId(500L);
            return app;
        });

        AppointmentDto result = bookingFacade.createBooking(10L, request);

        // 1. Kiểm tra DTO trả về
        assertNotNull(result);
        assertEquals(500L, result.getId());
        assertEquals("CONFIRMED", result.getStatus());
        assertEquals("Dau dau chong mat", result.getSymptoms());
        assertEquals(BigDecimal.valueOf(300000), result.getPrice());
        assertTrue(result.getBookingCode().startsWith("BK-"));

        // 2. Kiểm tra slot đã được chuyển thành không khả dụng và được lưu
        assertFalse(schedule.getIsAvailable());
        verify(scheduleRepository).save(schedule);

        // 3. Kiểm tra Appointment được lưu
        ArgumentCaptor<Appointment> appCaptor = ArgumentCaptor.forClass(Appointment.class);
        verify(appointmentRepository).save(appCaptor.capture());
        assertEquals("CONFIRMED", appCaptor.getValue().getStatus());
        assertEquals(doctor, appCaptor.getValue().getDoctor());
        assertEquals(patient, appCaptor.getValue().getPatient());

        // 4. Kiểm tra xóa cache Redis
        verify(redisCacheService).evictSlotCache(20L, schedule.getWorkDate());

        // 5. Kiểm tra phát sự kiện gửi email xác nhận
        verify(emailNotificationService).sendBookingConfirmation(any(Appointment.class));
    }

    @Test
    @DisplayName("Đặt lịch thất bại khi Schedule không tồn tại trong CSDL")
    void bookingFailsWhenScheduleNotFound() {
        CreateBookingDto request = new CreateBookingDto(999L, "Trieu chung");
        when(scheduleRepository.findByIdForUpdate(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookingFacade.createBooking(10L, request));
        verify(appointmentRepository, never()).save(any());
        verify(redisCacheService, never()).evictSlotCache(any(), any());
    }

    @Test
    @DisplayName("Đặt lịch thất bại khi slot đã bị người khác đặt trước (isAvailable = false)")
    void bookingFailsWhenScheduleUnavailable() {
        schedule.setIsAvailable(false);
        CreateBookingDto request = new CreateBookingDto(100L, "Trieu chung");
        when(scheduleRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(schedule));

        assertThrows(BookingConflictException.class, () -> bookingFacade.createBooking(10L, request));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đặt lịch thất bại khi slot có lịch hẹn trùng lặp trong appointmentRepository")
    void bookingFailsWhenAppointmentAlreadyExists() {
        CreateBookingDto request = new CreateBookingDto(100L, "Trieu chung");
        when(scheduleRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(schedule));
        when(appointmentRepository.existsByScheduleId(100L)).thenReturn(true);

        assertThrows(BookingConflictException.class, () -> bookingFacade.createBooking(10L, request));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đặt lịch thất bại khi khung giờ đã qua trong quá khứ")
    void bookingFailsWhenSlotInThePast() {
        schedule.setWorkDate(LocalDate.now().minusDays(1));
        CreateBookingDto request = new CreateBookingDto(100L, "Trieu chung");
        when(scheduleRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(schedule));

        assertThrows(AppException.class, () -> bookingFacade.createBooking(10L, request));
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Đặt lịch thất bại khi bác sĩ chưa được duyệt hoạt động")
    void bookingFailsWhenDoctorNotApproved() {
        doctor.setIsApproved(false);
        CreateBookingDto request = new CreateBookingDto(100L, "Trieu chung");
        when(scheduleRepository.findByIdForUpdate(100L)).thenReturn(Optional.of(schedule));
        when(appointmentRepository.existsByScheduleId(100L)).thenReturn(false);
        when(patientRepository.findById(10L)).thenReturn(Optional.of(patient));

        assertThrows(AppException.class, () -> bookingFacade.createBooking(10L, request));
        verify(appointmentRepository, never()).save(any());
    }
}

