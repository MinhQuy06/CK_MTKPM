package com.datlichkhambenh.backend;

import com.datlichkhambenh.backend.entity.*;
import com.datlichkhambenh.backend.messaging.EmailNotificationRequestedEvent;
import com.datlichkhambenh.backend.repository.*;
import com.datlichkhambenh.backend.security.JwtTokenProvider;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@RecordApplicationEvents
@Transactional
class ConsultationQuoteIntegrationTests {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired DoctorRepository doctors;
    @Autowired SpecialtyRepository specialties;
    @Autowired ScheduleRepository schedules;
    @Autowired PatientRepository patients;
    @Autowired AppointmentRepository appointments;
    @Autowired JwtTokenProvider jwt;
    @Autowired EntityManager em;
    @Autowired ApplicationEvents events;
    private Doctor doctor;
    private Schedule schedule;
    private User patientUser;
    private String token;

    @BeforeEach
    void fixture() {
        User doctorUser = users.save(User.builder().email("quote-doctor@test.vn")
                .passwordHash("unused").role("DOCTOR").isActive(true).build());
        Specialty specialty = specialties.save(Specialty.builder().name("Quote specialty").build());
        doctor = doctors.save(Doctor.builder().user(doctorUser).specialty(specialty)
                .fullName("Bác sĩ báo giá").consultationFee(new BigDecimal("300000.00"))
                .isApproved(true).build());
        schedule = schedules.save(Schedule.builder().doctor(doctor).workDate(LocalDate.now().plusDays(2))
                .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(9, 30)).isAvailable(true).build());
        patientUser = users.save(User.builder().email("quote-patient@test.vn")
                .passwordHash("unused").role("PATIENT").isActive(true).build());
        token = jwt.generateToken(patientUser.getEmail(), "PATIENT");
        em.flush();
    }

    @Test
    void patientGetsStandardQuoteWithoutReservationOrNotification() throws Exception {
        long before = appointments.count();
        for (int i = 0; i < 2; i++) {
            mvc.perform(quoteRequest().param("scheduleId", schedule.getId().toString())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.scheduleId").value(schedule.getId()))
                    .andExpect(jsonPath("$.data.strategyKey").value("STANDARD"))
                    .andExpect(jsonPath("$.data.strategyName").value("Tiêu chuẩn"))
                    .andExpect(jsonPath("$.data.basePrice").value(300000))
                    .andExpect(jsonPath("$.data.discountRate").value(0))
                    .andExpect(jsonPath("$.data.finalPrice").value(300000))
                    .andExpect(jsonPath("$.data.currency").value("VND"))
                    .andExpect(jsonPath("$.data.simulated").value(false));
        }
        em.flush();
        em.clear();
        assertEquals(before, appointments.count());
        assertTrue(schedules.findById(schedule.getId()).orElseThrow().getIsAvailable());
        assertEquals(0, events.stream(EmailNotificationRequestedEvent.class).count());
    }

    @ParameterizedTest
    @CsvSource({"0,0", "300000.49,300000", "300000.50,300001", "9999999999.49,9999999999"})
    void roundsToWholeVnd(String fee, BigDecimal expected) throws Exception {
        // Expected amounts are literal, independently hand-checked at HALF_UP boundaries.
        doctor.setConsultationFee(new BigDecimal(fee));
        mvc.perform(quoteRequest().param("scheduleId", schedule.getId().toString())
                        .param("strategyKey", "STANDARD").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.finalPrice").value(expected));
    }

    @Test
    void missingFeeIsNotFree() throws Exception {
        doctor.setConsultationFee(null);
        quoteStatus(400);
    }

    @ParameterizedTest
    @CsvSource({"-0.01", "9999999999.50"})
    void invalidOrRoundedOverflowFeeIsRejected(String fee) throws Exception {
        doctor.setConsultationFee(new BigDecimal(fee));
        quoteStatus(400);
    }

    @Test
    void unknownScheduleIsNotFound() throws Exception {
        mvc.perform(quoteRequest().param("scheduleId", "9223372036854775807")
                .header("Authorization", "Bearer " + token)).andExpect(status().isNotFound());
    }

    @Test
    void unavailableScheduleIsRejected() throws Exception {
        schedule.setIsAvailable(false);
        quoteStatus(409);
    }

    @Test
    void scheduleWithExistingAppointmentIsRejectedEvenIfFlagIsAvailable() throws Exception {
        Patient patient = patients.save(Patient.builder().user(patientUser).fullName("Patient").build());
        appointments.save(Appointment.builder().bookingCode("QUOTE-TAKEN").patient(patient)
                .doctor(doctor).schedule(schedule).status("CONFIRMED").price(new BigDecimal("300000")).build());
        quoteStatus(409);
    }

    @Test
    void pastScheduleIsRejected() throws Exception {
        schedule.setWorkDate(LocalDate.now().minusDays(1));
        quoteStatus(409);
    }

    @Test
    void pastSlotIsEvaluatedInVietnamTime() throws Exception {
        var past = java.time.LocalDateTime.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).minusMinutes(10);
        schedule.setWorkDate(past.toLocalDate());
        schedule.setStartTime(past.toLocalTime());
        quoteStatus(409);
    }

    @Test
    void unapprovedDoctorIsRejected() throws Exception {
        doctor.setIsApproved(false);
        quoteStatus(409);
    }

    @Test
    void inactiveDoctorIsRejected() throws Exception {
        doctor.getUser().setIsActive(false);
        quoteStatus(409);
    }

    @ParameterizedTest
    @CsvSource({"UNKNOWN", "INSURANCE", "VIP", "' '", "''"})
    void unsupportedOrBlankKeyIsRejected(String key) throws Exception {
        mvc.perform(quoteRequest().param("scheduleId", schedule.getId().toString())
                .param("strategyKey", key).header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @CsvSource({"0", "-1", "abc", "9223372036854775808"})
    void invalidScheduleIdIsRejected(String id) throws Exception {
        mvc.perform(quoteRequest().param("scheduleId", id)
                .header("Authorization", "Bearer " + token)).andExpect(status().isBadRequest());
    }

    @Test
    void scheduleIdIsRequired() throws Exception {
        mvc.perform(quoteRequest().header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void anonymousRequestIsUnauthorized() throws Exception {
        mvc.perform(quoteRequest().param("scheduleId", schedule.getId().toString()))
                .andExpect(status().isUnauthorized());
    }

    @ParameterizedTest
    @CsvSource({"DOCTOR", "ADMIN"})
    void otherRolesAreForbidden(String role) throws Exception {
        User actor = role.equals("DOCTOR") ? doctor.getUser() : patientUser;
        actor.setRole(role);
        em.flush();
        mvc.perform(quoteRequest().param("scheduleId", schedule.getId().toString())
                .header("Authorization", "Bearer " + jwt.generateToken(actor.getEmail(), role)))
                .andExpect(status().isForbidden());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder quoteRequest() {
        return get("/api/v1/appointments/quote").servletPath("/api/v1/appointments/quote");
    }

    private void quoteStatus(int expected) throws Exception {
        mvc.perform(quoteRequest().param("scheduleId", schedule.getId().toString())
                .header("Authorization", "Bearer " + token)).andExpect(status().is(expected));
    }
}
