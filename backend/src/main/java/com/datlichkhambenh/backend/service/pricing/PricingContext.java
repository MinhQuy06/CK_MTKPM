package com.datlichkhambenh.backend.service.pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Selects a registered algorithm and enforces common VND monetary boundaries. */
@Component
public class PricingContext {
    private static final BigDecimal MAX_STORED_PRICE = new BigDecimal("9999999999.99");
    private final Map<String, ConsultationPricingStrategy> strategies;

    public PricingContext(List<ConsultationPricingStrategy> strategies) {
        Map<String, ConsultationPricingStrategy> registered = new HashMap<>();
        for (ConsultationPricingStrategy strategy : strategies) {
            if (registered.putIfAbsent(strategy.key(), strategy) != null) {
                throw new IllegalArgumentException("Trùng khóa Strategy: " + strategy.key());
            }
        }
        this.strategies = Map.copyOf(registered);
    }

    public PricingAmounts calculate(String key, BigDecimal fee) {
        ConsultationPricingStrategy strategy = strategies.get(key == null ? "STANDARD" : key);
        if (strategy == null) {
            throw new IllegalArgumentException("Loại tính phí không được hỗ trợ");
        }
        BigDecimal rate = strategy.discountRate();
        if (rate == null || rate.signum() < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Tỷ lệ giảm phải nằm trong khoảng 0 đến 1");
        }
        BigDecimal base = wholeVnd(fee);
        BigDecimal price = wholeVnd(strategy.calculate(base));
        if (price.compareTo(base) > 0) {
            throw new IllegalArgumentException("Giá sau giảm không được vượt phí gốc");
        }
        return new PricingAmounts(strategy.key(), strategy.name(), base, rate, price, strategy.simulated());
    }

    private static BigDecimal wholeVnd(BigDecimal amount) {
        if (amount == null || amount.signum() < 0 || amount.compareTo(MAX_STORED_PRICE) > 0) {
            throw new IllegalArgumentException("Phí khám chưa được cấu hình hoặc không hợp lệ");
        }
        BigDecimal rounded = amount.setScale(0, RoundingMode.HALF_UP);
        if (rounded.compareTo(MAX_STORED_PRICE) > 0) {
            throw new IllegalArgumentException("Phí khám vượt giới hạn lưu trữ sau làm tròn");
        }
        return rounded;
    }
}
