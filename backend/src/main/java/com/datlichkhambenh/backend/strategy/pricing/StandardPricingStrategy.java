package com.datlichkhambenh.backend.strategy.pricing;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/** Concrete Strategy: loại phí Tiêu chuẩn, mặc định không giảm (cấu hình app.pricing.standard-discount-rate). */
@Component
public class StandardPricingStrategy implements ConsultationPricingStrategy {
    private final BigDecimal discountRate;

    public StandardPricingStrategy(@Value("${app.pricing.standard-discount-rate:0}") BigDecimal discountRate) {
        if (discountRate == null || discountRate.signum() < 0 || discountRate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Tỷ lệ giảm phải nằm trong khoảng 0 đến 1");
        }
        this.discountRate = discountRate;
    }

    @Override public String key() { return "STANDARD"; }
    @Override public String name() { return "Tiêu chuẩn"; }
    @Override public BigDecimal discountRate() { return discountRate; }
    @Override public boolean simulated() { return false; }

    @Override
    public BigDecimal calculate(BigDecimal basePrice) {
        return basePrice.multiply(BigDecimal.ONE.subtract(discountRate));
    }
}
