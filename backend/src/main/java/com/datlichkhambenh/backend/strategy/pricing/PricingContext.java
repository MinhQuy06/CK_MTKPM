package com.datlichkhambenh.backend.strategy.pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Context (GoF Strategy): giữ danh sách bất biến các Strategy do Spring tiêm vào, chọn đúng Strategy theo khóa
 * và áp các quy tắc tiền chung (tỷ lệ trong [0,1], làm tròn HALF_UP đến đồng, giới hạn cột DECIMAL).
 * Thêm loại phí mới: chỉ cần thêm một lớp @Component cài ConsultationPricingStrategy.
 */
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

    public boolean supports(String key) {
        return key != null && strategies.containsKey(key);
    }

    /** Các loại phí đã đăng ký: Tiêu chuẩn đứng đầu, còn lại theo khóa. */
    public List<PricingChoice> choices() {
        return strategies.values().stream()
                .map(s -> new PricingChoice(s.key(), s.name()))
                .sorted(Comparator.comparing((PricingChoice c) -> !"STANDARD".equals(c.key()))
                        .thenComparing(PricingChoice::key))
                .toList();
    }

    /** Tính giá theo loại phí; khóa null nghĩa là Tiêu chuẩn. */

    public PricingAmounts calculate(String key, BigDecimal fee) {
        ConsultationPricingStrategy strategy = strategies.get(key == null ? "STANDARD" : key);
        if (strategy == null) {
            throw new IllegalArgumentException("Loại tính phí không được hỗ trợ");
        }
        BigDecimal rate = strategy.discountRate();
        if (rate == null || rate.signum() < 0 || rate.compareTo(BigDecimal.ONE) > 0) {
            throw new IllegalArgumentException("Tỷ lệ giảm phải nằm trong khoảng 0 đến 1");
        }
        if (rate.stripTrailingZeros().scale() > 18) {
            throw new IllegalArgumentException("Tỷ lệ giảm có tối đa 18 chữ số thập phân");
        }
        BigDecimal base = wholeVnd(fee);
        BigDecimal price = wholeVnd(strategy.calculate(base));
        if (price.compareTo(base) > 0) {
            throw new IllegalArgumentException("Giá sau giảm không được vượt phí gốc");
        }
        return new PricingAmounts(strategy.key(), strategy.name(), base, rate, price, strategy.simulated());
    }

    /** Làm tròn HALF_UP đến đồng và kiểm tra giới hạn lưu trữ. */
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
