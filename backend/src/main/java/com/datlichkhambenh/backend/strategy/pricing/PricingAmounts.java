package com.datlichkhambenh.backend.strategy.pricing;

import java.math.BigDecimal;

/** Kết quả tính phí của Context: loại phí, phí gốc, tỷ lệ giảm và giá cuối (đã làm tròn đến đồng). */
public record PricingAmounts(String strategyKey, String strategyName, BigDecimal basePrice,
                             BigDecimal discountRate, BigDecimal finalPrice, boolean simulated) {
}
