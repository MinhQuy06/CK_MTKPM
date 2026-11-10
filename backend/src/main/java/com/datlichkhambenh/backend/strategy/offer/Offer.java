package com.datlichkhambenh.backend.strategy.offer;

import com.datlichkhambenh.backend.strategy.pricing.PricingAmounts;

import java.time.LocalDate;
import java.time.LocalTime;

/** Giá chào cho một ca đặt được (xem "Giá chào" trong GLOSSARY.md). Chỉ đọc, không khóa gì. */
public record Offer(Long scheduleId, PricingAmounts amounts, LocalDate workDate, LocalTime startTime, LocalTime endTime) {
}
