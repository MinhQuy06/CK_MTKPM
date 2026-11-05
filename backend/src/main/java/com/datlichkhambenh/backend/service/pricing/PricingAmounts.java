package com.datlichkhambenh.backend.service.pricing;

import java.math.BigDecimal;

public record PricingAmounts(String strategyKey, String strategyName, BigDecimal basePrice,
                             BigDecimal discountRate, BigDecimal finalPrice, boolean simulated) {}
