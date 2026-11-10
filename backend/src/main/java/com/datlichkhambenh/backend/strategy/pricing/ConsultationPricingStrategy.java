package com.datlichkhambenh.backend.strategy.pricing;

import java.math.BigDecimal;

/**
 * Strategy (GoF): một cách tính phí khám có thể thay thế cho nhau.
 * Tỷ lệ giảm là phần thập phân trong [0, 1]; làm tròn và kiểm tra giới hạn do PricingContext lo.
 */
public interface ConsultationPricingStrategy {
    /** Khóa định danh: STANDARD, INSURANCE, VIP… */
    String key();

    /** Tên hiển thị cho bệnh nhân. */
    String name();

    BigDecimal discountRate();

    /** Giá trước làm tròn, tính từ phí gốc đã chuẩn hóa. */
    BigDecimal calculate(BigDecimal basePrice);

    /** Giá mô phỏng hay giá thật (hiện mọi loại phí đều là giá thật). */
    boolean simulated();
}
