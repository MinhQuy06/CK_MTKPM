package com.datlichkhambenh.backend.strategy.offer;

import com.datlichkhambenh.backend.exception.BookingRejectionException;
import org.springframework.http.HttpStatus;

/**
 * Mọi lý do có tên khiến giá chào bị từ chối (xem "Lý do từ chối" trong GLOSSARY.md). Mã (code) là thứ giao diện
 * rẽ nhánh theo; status là mã HTTP của response.
 */
public enum BookingRejection {
    /** Ca không tồn tại (hoặc đã bị xóa). */
    SLOT_MISSING("SLOT_UNAVAILABLE", HttpStatus.NOT_FOUND, "Ca khám không tồn tại. Vui lòng chọn lại ca."),
    /** Ca còn đó nhưng đã kín, đã bắt đầu, hoặc đổi ngày giờ so với giá đã đồng ý. */
    SLOT_UNAVAILABLE("SLOT_UNAVAILABLE", HttpStatus.CONFLICT, "Ca khám không còn khả dụng. Vui lòng chọn ca khác."),
    PRICE_CHANGED("PRICE_CHANGED", HttpStatus.CONFLICT, "Giá khám đã thay đổi. Vui lòng xem lại và xác nhận giá mới."),
    BENEFIT_UNAVAILABLE("BENEFIT_UNAVAILABLE", HttpStatus.FORBIDDEN,
            "Bạn không đủ điều kiện hưởng loại phí này cho ngày khám đã chọn"),
    UNSUPPORTED_STRATEGY("UNSUPPORTED_STRATEGY", HttpStatus.BAD_REQUEST, "Loại phí không được hỗ trợ"),
    DOCTOR_UNAVAILABLE("DOCTOR_UNAVAILABLE", HttpStatus.CONFLICT, "Bác sĩ hiện không nhận lịch khám"),
    /** Route xác nhận được gọi mà thiếu một phần giá đã đồng ý. */
    QUOTE_REQUIRED("QUOTE_REQUIRED", HttpStatus.BAD_REQUEST, "Cần báo giá đầy đủ trước khi xác nhận");

    private final String code;
    private final HttpStatus status;
    private final String defaultMessage;

    BookingRejection(String code, HttpStatus status, String defaultMessage) {
        this.code = code;
        this.status = status;
        this.defaultMessage = defaultMessage;
    }

    public String code() {
        return code;
    }

    public HttpStatus status() {
        return status;
    }

    public BookingRejectionException exception() {
        return exception(defaultMessage);
    }

    public BookingRejectionException exception(String message) {
        return new BookingRejectionException(code, message, status);
    }
}
