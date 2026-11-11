package com.datlichkhambenh.backend.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Profile prod: email là kênh thông báo chính, nên thiếu tài khoản SMTP thì dừng ngay khi khởi động. */
@Component
@Profile("prod")
public class MailSettingsValidator implements InitializingBean {
    private final String username;
    private final String password;

    public MailSettingsValidator(@Value("${spring.mail.username}") String username,
                                 @Value("${spring.mail.password}") String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public void afterPropertiesSet() {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new IllegalStateException("Thiếu MAIL_USERNAME hoặc MAIL_PASSWORD: cần tài khoản SMTP để gửi email thông báo.");
        }
    }
}
