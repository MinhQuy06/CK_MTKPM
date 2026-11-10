package com.datlichkhambenh.backend.strategy.offer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * Điều bệnh nhân đã đồng ý trước khi đặt lịch. Facade chọn dạng theo route; {@link BookingOffer} so giá chào đã khóa
 * với nó. Trường có thể null: phần thiếu được báo {@link BookingRejection#QUOTE_REQUIRED} sau các kiểm tra ca,
 * để thứ tự ưu tiên lỗi giữ như cũ.
 */
public sealed interface Expectation {

    /** Route xác nhận: giá đã đồng ý, kể cả ngày giờ ca (xem "Giá đã đồng ý" trong GLOSSARY.md). */
    record ConfirmedQuote(String pricingKey, BigDecimal basePrice, BigDecimal discountRate, BigDecimal finalPrice,
                          LocalDate workDate, LocalTime startTime, LocalTime endTime) implements Expectation {
    }

    /** Route cũ có kèm giá: so số tiền, không so ngày giờ ca. */
    record LegacyQuote(String pricingKey, BigDecimal basePrice, BigDecimal discountRate, BigDecimal finalPrice)
            implements Expectation {
    }

    /** Route cũ không kèm báo giá: chỉ loại phí Tiêu chuẩn (xem "Đặt lịch kiểu cũ" trong GLOSSARY.md). */
    record LegacyStandard() implements Expectation {
    }
}
