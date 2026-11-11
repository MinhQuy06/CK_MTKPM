package com.datlichkhambenh.backend.config;

import com.datlichkhambenh.backend.entity.User;
import com.datlichkhambenh.backend.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Profile prod: không nạp dữ liệu mẫu; chỉ tạo đúng một tài khoản Admin đầu tiên từ biến môi trường
 * khi hệ thống chưa có Admin nào đang hoạt động. Chạy lại không tạo trùng.
 */
@Slf4j
@Component
@Profile("prod")
public class AdminBootstrap implements ApplicationRunner {
    static final int MIN_PASSWORD_LENGTH = 8;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String email;
    private final String password;

    public AdminBootstrap(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          @Value("${app.bootstrap.admin-email}") String email,
                          @Value("${app.bootstrap.admin-password}") String password) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.email = email == null ? "" : email.trim();
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.countByRoleAndIsActiveTrue("ADMIN") > 0) {
            log.info("[Bootstrap] Đã có Admin, bỏ qua tạo tài khoản đầu tiên.");
            return;
        }
        if (email.isEmpty() || !email.contains("@")) {
            throw new IllegalStateException("ADMIN_EMAIL không hợp lệ: cần email của Admin đầu tiên.");
        }
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalStateException("ADMIN_PASSWORD phải có ít nhất " + MIN_PASSWORD_LENGTH + " ký tự.");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new IllegalStateException("ADMIN_EMAIL đã thuộc một tài khoản không phải Admin đang hoạt động: " + email);
        }
        userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role("ADMIN")
                .isActive(true)
                .build());
        log.info("[Bootstrap] Đã tạo Admin đầu tiên: {}", email);
    }
}
