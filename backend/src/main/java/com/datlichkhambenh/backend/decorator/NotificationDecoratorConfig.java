package com.datlichkhambenh.backend.decorator;

import com.datlichkhambenh.backend.adapter.NotificationChannel;
import com.datlichkhambenh.backend.builder.NotificationMessageDirector;
import com.datlichkhambenh.backend.service.AsyncEmailNotificationService;
import com.datlichkhambenh.backend.service.impl.AsyncEmailNotificationServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * The one place where the notification decorators are stacked. Everything that injects
 * {@link AsyncEmailNotificationService} receives this primary, decorated bean.
 */
@Configuration
public class NotificationDecoratorConfig {

    @Bean
    @Primary
    public AsyncEmailNotificationService decoratedNotificationService(AsyncEmailNotificationServiceImpl email,
                                                                      NotificationLogRepository history,
                                                                      NotificationChannel sms,
                                                                      NotificationMessageDirector director) {
        // Outermost first: dedup → SMS → audit → e-mail
        return new DeduplicationDecorator(
                new SmsNotificationDecorator(
                        new AuditLogDecorator(email, history),
                        sms, director, history),
                history);
    }
}
