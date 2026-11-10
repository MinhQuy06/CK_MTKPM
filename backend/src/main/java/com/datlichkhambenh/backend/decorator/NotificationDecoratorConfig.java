package com.datlichkhambenh.backend.decorator;

import com.datlichkhambenh.backend.adapter.NotificationChannel;
import com.datlichkhambenh.backend.builder.NotificationMessageDirector;
import com.datlichkhambenh.backend.service.AsyncEmailNotificationService;
import com.datlichkhambenh.backend.service.impl.AsyncEmailNotificationServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Nơi duy nhất xếp chồng các decorator thông báo. Mọi chỗ tiêm {@link AsyncEmailNotificationService}
 * đều nhận bean @Primary đã bọc này.
 */
@Configuration
public class NotificationDecoratorConfig {

    @Bean
    @Primary
    public AsyncEmailNotificationService decoratedNotificationService(AsyncEmailNotificationServiceImpl email,
                                                                      NotificationLogRepository history,
                                                                      NotificationChannel sms,
                                                                      NotificationMessageDirector director) {
        // Từ ngoài vào trong: chống trùng → SMS → ghi lịch sử → email gốc
        return new DeduplicationDecorator(
                new SmsNotificationDecorator(
                        new AuditLogDecorator(email, history),
                        sms, director, history),
                history);
    }
}
