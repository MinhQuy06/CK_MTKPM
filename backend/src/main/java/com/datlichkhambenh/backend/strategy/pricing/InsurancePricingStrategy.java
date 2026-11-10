package com.datlichkhambenh.backend.strategy.pricing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Concrete Strategy: loại phí Bảo hiểm, mặc định giảm 80% (cấu hình app.pricing.insurance-discount-rate). */
@Component
public class InsurancePricingStrategy implements ConsultationPricingStrategy {
    private final BigDecimal rate;

    public InsurancePricingStrategy(@Value("${app.pricing.insurance-discount-rate:0.8}") BigDecimal rate) {
        if (rate == null || rate.signum() < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Tỷ lệ giảm phải trong [0,1]");
        }
        this.rate = rate;
    }

    @Override public String key() { return "INSURANCE"; }
    @Override public String name() { return "Bảo hiểm"; }
    @Override public BigDecimal discountRate() { return rate; }
    @Override public boolean simulated() { return false; }

    /** Giá cuối = phí gốc × (1 − tỷ lệ giảm); Context lo làm tròn. */
    @Override
    public BigDecimal calculate(BigDecimal basePrice) {
        return basePrice.multiply(BigDecimal.ONE.subtract(rate));
    }
}
