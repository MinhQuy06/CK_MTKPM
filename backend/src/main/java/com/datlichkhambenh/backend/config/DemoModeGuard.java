package com.datlichkhambenh.backend.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Profile demo: dữ liệu mẫu chứa email và số điện thoại trông như thật (ví dụ bn.hoangminh@gmail.com).
 * Để không gửi nhầm thư/SMS cho người lạ, chế độ demo chỉ cho phép SMTP nội bộ (Mailpit) và SMS tắt.
 * Muốn gửi thật: bỏ profile demo (xem docs/production.md).
 */
@Component
@Profile("demo")
public class DemoModeGuard implements InitializingBean {
    static final Set<String> LOCAL_MAIL_HOSTS = Set.of("mailpit", "localhost", "127.0.0.1", "host.docker.internal");

    private final String mailHost;
    private final boolean smsEnabled;

    public DemoModeGuard(@Value("${spring.mail.host:}") String mailHost,
                         @Value("${app.sms.enabled:false}") boolean smsEnabled) {
        this.mailHost = mailHost;
        this.smsEnabled = smsEnabled;
    }

    @Override
    public void afterPropertiesSet() {
        if (smsEnabled) {
            throw new IllegalStateException("Chế độ demo không cho bật SMS thật (SMS_ENABLED=true): số điện thoại mẫu có thể là số thật. "
                    + "Bỏ 'demo' khỏi APP_PROFILES để gửi SMS thật.");
        }
        if (!LOCAL_MAIL_HOSTS.contains(mailHost == null ? "" : mailHost.trim().toLowerCase())) {
            throw new IllegalStateException("Chế độ demo chỉ gửi email vào SMTP nội bộ (Mailpit), không phải '" + mailHost
                    + "': email mẫu có thể là địa chỉ thật. Bỏ 'demo' khỏi APP_PROFILES để gửi email thật.");
        }
    }
}
