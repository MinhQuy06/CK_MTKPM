package com.datlichkhambenh.backend.exception;

import org.springframework.http.HttpStatus;

/**
 * Lỗi từ chối báo giá / đặt lịch có mã (SLOT_UNAVAILABLE, PRICE_CHANGED…) để giao diện rẽ nhánh.
 * Tạo qua enum BookingRejection; GlobalExceptionHandler đưa mã vào trường "code" của response.
 */
public class BookingRejectionException extends AppException {
    private final String code;

    public BookingRejectionException(String code, String message, HttpStatus status) {
        super(message, status);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
