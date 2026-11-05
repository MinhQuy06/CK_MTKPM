package com.datlichkhambenh.backend.service.pricing;

import java.math.BigDecimal;

/** An interchangeable consultation fee algorithm. Rates are discount fractions in [0, 1]. */
public interface ConsultationPricingStrategy {
    String key();
    String name();
    BigDecimal discountRate();
    BigDecimal calculate(BigDecimal basePrice);
    boolean simulated();
}
