package com.datlichkhambenh.backend.strategy.offer;

import com.datlichkhambenh.backend.entity.Doctor;
import com.datlichkhambenh.backend.entity.Schedule;
import com.datlichkhambenh.backend.strategy.entity.BenefitRequest;
import com.datlichkhambenh.backend.strategy.pricing.PricingAmounts;

/**
 * A booking offer that matched the patient's expectation, with its slot, doctor and (if any) benefit row-locked
 * until the caller's transaction ends. {@code benefit} is null for Standard pricing.
 */
public record LockedOffer(Schedule slot, Doctor doctor, BenefitRequest benefit, PricingAmounts amounts) {
}
