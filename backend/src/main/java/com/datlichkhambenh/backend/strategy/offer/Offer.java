package com.datlichkhambenh.backend.strategy.offer;

import com.datlichkhambenh.backend.strategy.pricing.PricingAmounts;

import java.time.LocalDate;
import java.time.LocalTime;

/** A booking offer for one bookable slot (see "Booking offer" in GLOSSARY.md). Read-only: nothing is locked. */
public record Offer(Long scheduleId, PricingAmounts amounts, LocalDate workDate, LocalTime startTime, LocalTime endTime) {
}
