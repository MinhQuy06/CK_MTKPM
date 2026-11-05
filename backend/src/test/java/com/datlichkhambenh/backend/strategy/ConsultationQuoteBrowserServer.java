package com.datlichkhambenh.backend.strategy;
import com.datlichkhambenh.backend.BackendApplication;

import com.datlichkhambenh.backend.entity.*;
import com.datlichkhambenh.backend.repository.*;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/** Standalone, test-classpath-only H2 fixture for the browser smoke test. */
public class ConsultationQuoteBrowserServer {
    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(BackendApplication.class,
                "--spring.profiles.active=test", "--server.port=" + (args.length == 0 ? "18081" : args[0]),
                "--logging.level.root=WARN", "--logging.level.com.datlichkhambenh=INFO");
        new TransactionTemplate(context.getBean(PlatformTransactionManager.class)).executeWithoutResult(transaction -> seed(context));
        System.out.println("QUOTE_BROWSER_FIXTURE_READY");
    }

    private static void seed(ConfigurableApplicationContext context) {
        UserRepository users = context.getBean(UserRepository.class);
        PasswordEncoder encoder = context.getBean(PasswordEncoder.class);
        Specialty specialty = context.getBean(SpecialtyRepository.class).save(
                Specialty.builder().name("Nội tổng quát").build());
        for (int i = 1; i <= 2; i++) {
            User user = users.save(User.builder().email("quote-doctor-" + i + "@test.vn")
                    .passwordHash(encoder.encode("BrowserTest123!")).role("DOCTOR").isActive(true).build());
            Doctor doctor = context.getBean(DoctorRepository.class).save(Doctor.builder().user(user)
                    .specialty(specialty).fullName("Bác sĩ Báo Giá " + i).degree("Thạc sĩ")
                    .experienceYears(5).consultationFee(new BigDecimal(i == 1 ? "300000" : "450000"))
                    .isApproved(true).build());
            for (int hour = 9; hour <= 10; hour++) {
                context.getBean(ScheduleRepository.class).save(Schedule.builder().doctor(doctor)
                        .workDate(LocalDate.now(java.time.ZoneId.of("Asia/Ho_Chi_Minh")).plusDays(1)).startTime(LocalTime.of(hour, 0))
                        .endTime(LocalTime.of(hour, 30)).isAvailable(true).build());
            }
        }
        users.save(User.builder().email("quote-admin@test.vn").passwordHash(encoder.encode("BrowserTest123!"))
                .role("ADMIN").isActive(true).build());
        User user = users.save(User.builder().email("quote-patient@test.vn")
                .passwordHash(encoder.encode("BrowserTest123!")).role("PATIENT").isActive(true).build());
        context.getBean(PatientRepository.class).save(Patient.builder().user(user).fullName("Bệnh nhân kiểm thử").build());
    }
}
