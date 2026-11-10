package com.datlichkhambenh.backend.support;

import com.datlichkhambenh.backend.entity.Doctor;
import com.datlichkhambenh.backend.entity.Patient;
import com.datlichkhambenh.backend.entity.Schedule;
import com.datlichkhambenh.backend.entity.Specialty;
import com.datlichkhambenh.backend.entity.User;
import com.datlichkhambenh.backend.messaging.EmailNotificationMessage;
import com.datlichkhambenh.backend.repository.DoctorRepository;
import com.datlichkhambenh.backend.repository.PatientRepository;
import com.datlichkhambenh.backend.repository.ScheduleRepository;
import com.datlichkhambenh.backend.repository.SpecialtyRepository;
import com.datlichkhambenh.backend.repository.UserRepository;
import com.datlichkhambenh.backend.security.JwtTokenProvider;
import com.datlichkhambenh.backend.service.impl.NoOpRedisCacheService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Seam for the design-pattern tickets: drive booking through the HTTP API and observe what leaves
 * the system (e-mails captured at RabbitTemplate). Each test gets a fresh patient, doctor and slot.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(BookingApiFixture.Capture.class)
public abstract class BookingApiFixture {
    protected static final String PATH = "/api/v1/appointments";
    protected static final BigDecimal FEE = new BigDecimal("300000");

    @Autowired protected MockMvc mvc;
    @Autowired protected ObjectMapper json;
    @Autowired protected JwtTokenProvider jwt;
    @Autowired protected UserRepository users;
    @Autowired protected PatientRepository patients;
    @Autowired protected DoctorRepository doctors;
    @Autowired protected SpecialtyRepository specialties;
    @Autowired protected ScheduleRepository schedules;
    @Autowired protected PlatformTransactionManager tm;
    @Autowired protected CapturingRabbitTemplate sink;
    @Autowired protected RecordingCache cache;

    protected String patientToken;
    protected String patientEmail;
    protected String patientPhone;
    protected Long patientId;
    protected Long doctorId;
    protected Long scheduleId;
    protected LocalDate day;

    @TestConfiguration
    public static class Capture {
        @Bean
        @Primary
        CapturingRabbitTemplate capturingRabbitTemplate(ConnectionFactory factory) {
            return new CapturingRabbitTemplate(factory);
        }

        @Bean
        @Primary
        RecordingCache recordingCache() {
            return new RecordingCache();
        }
    }

    /** Records slot-cache evictions as "doctorId@date" so tests can see cache reactions. */
    public static class RecordingCache extends NoOpRedisCacheService {
        public final List<String> slotEvictions = new CopyOnWriteArrayList<>();

        @Override
        public void evictSlotCache(Long doctorId, LocalDate date) {
            slotEvictions.add(doctorId + "@" + date);
        }
    }

    public static class CapturingRabbitTemplate extends RabbitTemplate {
        public final BlockingQueue<EmailNotificationMessage> messages = new LinkedBlockingQueue<>();

        CapturingRabbitTemplate(ConnectionFactory factory) {
            super(factory);
        }

        @Override
        public void convertAndSend(String exchange, String routingKey, Object message) {
            messages.add((EmailNotificationMessage) message);
        }
    }

    /** Override to seed a patient with a (unique, Vietnamese-format) phone number. */
    protected boolean patientHasPhone() {
        return false;
    }

    @BeforeEach
    void seedBookingWorld() {
        sink.messages.clear();
        cache.slotEvictions.clear();
        String suffix = UUID.randomUUID().toString();
        day = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(2);
        new TransactionTemplate(tm).executeWithoutResult(tx -> {
            patientEmail = "pattern-p-" + suffix + "@test.vn";
            User p = users.save(User.builder().email(patientEmail).passwordHash("unused")
                    .role("PATIENT").isActive(true).build());
            patientPhone = patientHasPhone()
                    ? "09" + String.format("%08d", ThreadLocalRandom.current().nextInt(100_000_000)) : null;
            patients.save(Patient.builder().user(p).fullName("Pattern Patient").phone(patientPhone).build());
            patientId = p.getId();
            patientToken = jwt.generateToken(p.getEmail(), "PATIENT");

            User d = users.save(User.builder().email("pattern-d-" + suffix + "@test.vn").passwordHash("unused")
                    .role("DOCTOR").isActive(true).build());
            Specialty specialty = specialties.save(Specialty.builder().name("Pattern " + suffix).build());
            Doctor doctor = doctors.save(Doctor.builder().user(d).specialty(specialty).fullName("Pattern Doctor")
                    .consultationFee(FEE).isApproved(true).build());
            doctorId = doctor.getUserId();
            scheduleId = schedules.save(Schedule.builder().doctor(doctor).workDate(day)
                    .startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(9, 30)).isAvailable(true).build()).getId();
        });
    }

    /** Standard-price confirmation body matching the seeded slot. */
    protected Map<String, Object> confirmBody() {
        Map<String, Object> body = new HashMap<>();
        body.put("scheduleId", scheduleId);
        body.put("symptoms", "Đau đầu");
        body.put("expectedWorkDate", day.toString());
        body.put("expectedStartTime", "09:00:00");
        body.put("expectedEndTime", "09:30:00");
        body.put("strategyKey", "STANDARD");
        body.put("expectedBasePrice", 300000);
        body.put("expectedDiscountRate", 0);
        body.put("expectedPrice", 300000);
        return body;
    }

    /** Books the seeded slot and returns the saved appointment JSON. */
    protected JsonNode book() throws Exception {
        String path = PATH + "/confirm";
        var response = mvc.perform(post(path).servletPath(path).header("Authorization", "Bearer " + patientToken)
                .contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(confirmBody())))
                .andReturn().getResponse();
        assertEquals(201, response.getStatus(), response.getContentAsString());
        return json.readTree(response.getContentAsString()).path("data");
    }

    protected void cancel(long appointmentId) throws Exception {
        String path = PATH + "/" + appointmentId + "/cancel";
        var response = mvc.perform(delete(path).servletPath(path).header("Authorization", "Bearer " + patientToken))
                .andReturn().getResponse();
        assertEquals(200, response.getStatus(), response.getContentAsString());
    }

    /** Next e-mail that left the system after commit. */
    protected EmailNotificationMessage nextEmail() throws InterruptedException {
        EmailNotificationMessage message = sink.messages.poll(3, TimeUnit.SECONDS);
        assertNotNull(message, "after-commit notification missing");
        return message;
    }

    protected EmailNotificationMessage noMoreEmailWithin(long millis) throws InterruptedException {
        return sink.messages.poll(millis, TimeUnit.MILLISECONDS);
    }
}
