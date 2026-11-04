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
