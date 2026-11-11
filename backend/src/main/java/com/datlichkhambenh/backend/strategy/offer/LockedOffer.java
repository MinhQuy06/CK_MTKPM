package com.datlichkhambenh.backend.strategy.offer;

import com.datlichkhambenh.backend.entity.Doctor;
import com.datlichkhambenh.backend.entity.Schedule;
import com.datlichkhambenh.backend.strategy.entity.BenefitRequest;
import com.datlichkhambenh.backend.strategy.pricing.PricingAmounts;

/**
 * Giá chào đã khớp kỳ vọng của bệnh nhân; ca, bác sĩ và quyền hưởng (nếu có) đang bị khóa dòng đến hết giao dịch
 * của nơi gọi. {@code benefit} là null với loại phí Tiêu chuẩn.
 */
public record LockedOffer(Schedule slot, Doctor doctor, BenefitRequest benefit, PricingAmounts amounts) {
}
