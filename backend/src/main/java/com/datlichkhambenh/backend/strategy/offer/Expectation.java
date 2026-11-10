package com.datlichkhambenh.backend.strategy.offer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * What the patient agreed to before asking to book. The Facade picks the variant from the route; the
 * {@link BookingOffer} module checks the locked offer against it. Fields may be null: missing ones are
 * reported as {@link BookingRejection#QUOTE_REQUIRED} after the slot checks, so error precedence stays stable.
 */
public sealed interface Expectation {

    /** Confirm route: the agreed quote including the slot's day and times (see "Agreed quote" in GLOSSARY.md). */
    record ConfirmedQuote(String pricingKey, BigDecimal basePrice, BigDecimal discountRate, BigDecimal finalPrice,
                          LocalDate workDate, LocalTime startTime, LocalTime endTime) implements Expectation {
    }

    /** Old route carrying prices: amounts are compared, the slot's day and times are not. */
    record LegacyQuote(String pricingKey, BigDecimal basePrice, BigDecimal discountRate, BigDecimal finalPrice)
            implements Expectation {
    }

    /** Old route without any quote: Standard pricing only (see "Legacy booking" in GLOSSARY.md). */
    record LegacyStandard() implements Expectation {
    }
}
