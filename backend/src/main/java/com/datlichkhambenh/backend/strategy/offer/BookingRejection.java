package com.datlichkhambenh.backend.strategy.offer;

import com.datlichkhambenh.backend.exception.BookingRejectionException;
import org.springframework.http.HttpStatus;

/**
 * Every named reason a booking offer can be refused (see "Booking rejection" in GLOSSARY.md). The code is what
 * the web UI branches on; the status is the HTTP status of the response.
 */
public enum BookingRejection {
    /** The slot does not exist (any more). */
    SLOT_MISSING("SLOT_UNAVAILABLE", HttpStatus.NOT_FOUND, "Ca khám không tồn tại. Vui lòng chọn lại ca."),
    /** The slot exists but is taken, already started, or its day/time changed since the agreed quote. */
    SLOT_UNAVAILABLE("SLOT_UNAVAILABLE", HttpStatus.CONFLICT, "Ca khám không còn khả dụng. Vui lòng chọn ca khác."),
    PRICE_CHANGED("PRICE_CHANGED", HttpStatus.CONFLICT, "Giá khám đã thay đổi. Vui lòng xem lại và xác nhận giá mới."),
    BENEFIT_UNAVAILABLE("BENEFIT_UNAVAILABLE", HttpStatus.FORBIDDEN,
            "Bạn không đủ điều kiện hưởng loại phí này cho ngày khám đã chọn"),
    UNSUPPORTED_STRATEGY("UNSUPPORTED_STRATEGY", HttpStatus.BAD_REQUEST, "Loại phí không được hỗ trợ"),
    DOCTOR_UNAVAILABLE("DOCTOR_UNAVAILABLE", HttpStatus.CONFLICT, "Bác sĩ hiện không nhận lịch khám"),
    /** The confirm route was called without the full agreed quote. */
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
