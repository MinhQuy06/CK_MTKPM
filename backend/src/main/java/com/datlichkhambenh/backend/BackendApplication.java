package com.datlichkhambenh.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Entry point của DatLichKhamBenh Backend.
 *
 * @EnableAsync — bật xử lý bất đồng bộ cho AsyncEmailNotificationService
 *              (gửi email xác nhận / nhắc lịch không block luồng đặt lịch).
 */
@SpringBootApplication
@EnableAsync
public class BackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(BackendApplication.class, args);
    }
}
