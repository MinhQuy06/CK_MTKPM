# Mã nguồn Observer — bản chụp từ backend

Các file dưới đây được sao chép nguyên văn để đối chiếu. Chúng phụ thuộc backend Spring Boot và không phải chương trình Java độc lập. Code triển khai chính vẫn ở backend.

## AsyncEmailNotificationService.java

Nguồn: [AsyncEmailNotificationService.java](../../../backend/src/main/java/com/datlichkhambenh/backend/service/AsyncEmailNotificationService.java).

```java
package com.datlichkhambenh.backend.service;

import com.datlichkhambenh.backend.entity.Appointment;

public interface AsyncEmailNotificationService {
    void sendBookingConfirmation(Appointment appointment);
    void sendAppointmentReminder(Appointment appointment);
    void sendCancellationNotice(Appointment appointment);
}
```

## AsyncEmailNotificationServiceImpl.java

Nguồn: [AsyncEmailNotificationServiceImpl.java](../../../backend/src/main/java/com/datlichkhambenh/backend/service/impl/AsyncEmailNotificationServiceImpl.java).

```java
package com.datlichkhambenh.backend.service.impl;

import com.datlichkhambenh.backend.entity.Appointment;
import com.datlichkhambenh.backend.messaging.EmailNotificationMessage;
import com.datlichkhambenh.backend.messaging.EmailNotificationRequestedEvent;
import com.datlichkhambenh.backend.service.AsyncEmailNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AsyncEmailNotificationServiceImpl implements AsyncEmailNotificationService {
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public void sendBookingConfirmation(Appointment appointment) {
        publish("BOOKING_CONFIRMED", appointment);
    }

    @Override
    public void sendAppointmentReminder(Appointment appointment) {
        publish("APPOINTMENT_REMINDER", appointment);
    }

    @Override
    public void sendCancellationNotice(Appointment appointment) {
        publish("BOOKING_CANCELLED", appointment);
    }

    private void publish(String type, Appointment appointment) {
        EmailNotificationMessage message = new EmailNotificationMessage(
                type,
                appointment.getPatient().getUser().getEmail(),
                appointment.getPatient().getFullName(),
                appointment.getBookingCode(),
                appointment.getDoctor().getFullName(),
                appointment.getSchedule().getWorkDate().toString(),
                appointment.getSchedule().getStartTime().toString(),
                appointment.getSchedule().getEndTime().toString(),
                appointment.getPrice() == null ? null : appointment.getPrice().toPlainString()
        );
        eventPublisher.publishEvent(new EmailNotificationRequestedEvent(message));
    }
}
```

## EmailNotificationMessage.java

Nguồn: [EmailNotificationMessage.java](../../../backend/src/main/java/com/datlichkhambenh/backend/messaging/EmailNotificationMessage.java).

```java
package com.datlichkhambenh.backend.messaging;

public record EmailNotificationMessage(
        String type,
        String recipientEmail,
        String patientName,
        String bookingCode,
        String doctorName,
        String workDate,
        String startTime,
        String endTime,
        String price
) {
}
```

## EmailNotificationRequestedEvent.java

Nguồn: [EmailNotificationRequestedEvent.java](../../../backend/src/main/java/com/datlichkhambenh/backend/messaging/EmailNotificationRequestedEvent.java).

```java
package com.datlichkhambenh.backend.messaging;

public record EmailNotificationRequestedEvent(EmailNotificationMessage message) {
}
```

## EmailQueuePublisher.java

Nguồn: [EmailQueuePublisher.java](../../../backend/src/main/java/com/datlichkhambenh/backend/messaging/EmailQueuePublisher.java).

```java
package com.datlichkhambenh.backend.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailQueuePublisher {
    private final RabbitTemplate rabbitTemplate;

    @Value("${app.messaging.email-exchange}")
    private String exchange;

    @Value("${app.messaging.email-routing-key}")
    private String routingKey;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void publishAfterCommit(EmailNotificationRequestedEvent event) {
        try {
            rabbitTemplate.convertAndSend(exchange, routingKey, event.message());
            log.info("Queued email notification for booking {}", event.message().bookingCode());
        } catch (Exception ex) {
            log.error("Cannot queue email notification for booking {}: {}",
                    event.message().bookingCode(), ex.getMessage());
        }
    }
}
```

## EmailQueueConsumer.java

Nguồn: [EmailQueueConsumer.java](../../../backend/src/main/java/com/datlichkhambenh/backend/messaging/EmailQueueConsumer.java).

```java
package com.datlichkhambenh.backend.messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailQueueConsumer {
    private final JavaMailSender mailSender;

    @RabbitListener(queues = "${app.messaging.email-queue}")
    public void consume(EmailNotificationMessage notification) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(notification.recipientEmail());
        message.setSubject(subject(notification));
        message.setText(body(notification));
        mailSender.send(message);
        log.info("Sent {} email for booking {}", notification.type(), notification.bookingCode());
    }

    private String subject(EmailNotificationMessage notification) {
        return switch (notification.type()) {
            case "BOOKING_CANCELLED" -> "Thông báo hủy lịch khám - Mã: " + notification.bookingCode();
            case "APPOINTMENT_REMINDER" -> "Nhắc lịch khám - Mã: " + notification.bookingCode();
            default -> "Xác nhận đặt lịch khám - Mã: " + notification.bookingCode();
        };
    }

    private String body(EmailNotificationMessage n) {
        if ("BOOKING_CANCELLED".equals(n.type())) {
            return "Kính gửi %s,\n\nLịch khám mã %s đã được hủy thành công."
                    .formatted(n.patientName(), n.bookingCode());
        }
        String prefix = "APPOINTMENT_REMINDER".equals(n.type())
                ? "Đây là thông báo nhắc lịch khám của bạn."
                : "Bạn đã đặt lịch khám thành công.";
        return ("Kính gửi %s,\n\n%s\nMã đặt chỗ: %s\nBác sĩ: %s\nNgày khám: %s\n"
                + "Thời gian: %s - %s\nPhí khám: %s VNĐ")
                .formatted(n.patientName(), prefix, n.bookingCode(), n.doctorName(), n.workDate(),
                        n.startTime(), n.endTime(), n.price());
    }
}
```

## RabbitMqConfig.java

Nguồn: [RabbitMqConfig.java](../../../backend/src/main/java/com/datlichkhambenh/backend/config/RabbitMqConfig.java).

```java
package com.datlichkhambenh.backend.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {
    @Bean
    Queue emailQueue(@Value("${app.messaging.email-queue}") String queueName) {
        return new Queue(queueName, true);
    }

    @Bean
    DirectExchange emailExchange(@Value("${app.messaging.email-exchange}") String exchangeName) {
        return new DirectExchange(exchangeName, true, false);
    }

    @Bean
    Binding emailBinding(Queue emailQueue, DirectExchange emailExchange,
                         @Value("${app.messaging.email-routing-key}") String routingKey) {
        return BindingBuilder.bind(emailQueue).to(emailExchange).with(routingKey);
    }

    @Bean
    Jackson2JsonMessageConverter rabbitMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory,
                                  Jackson2JsonMessageConverter rabbitMessageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(rabbitMessageConverter);
        return template;
    }
}
```

## AsyncConfig.java

Nguồn: [AsyncConfig.java](../../../backend/src/main/java/com/datlichkhambenh/backend/config/AsyncConfig.java).

```java
package com.datlichkhambenh.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * AsyncConfig — Cấu hình ThreadPool cho @Async.
 *
 * Dùng bởi AsyncEmailNotificationService để gửi email
 * xác nhận / nhắc lịch mà không block luồng đặt lịch.
 *
 * Thread pool tách biệt với default executor để tránh
 * ảnh hưởng đến các task async khác trong hệ thống.
 */
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Bean(name = "emailTaskExecutor")
    public Executor emailTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("email-async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
```

