package com.datlichkhambenh.backend.strategy.offer;

import com.datlichkhambenh.backend.entity.Doctor;
import com.datlichkhambenh.backend.entity.Schedule;
import com.datlichkhambenh.backend.repository.AppointmentRepository;
import com.datlichkhambenh.backend.repository.DoctorRepository;
import com.datlichkhambenh.backend.repository.ScheduleRepository;
import com.datlichkhambenh.backend.strategy.entity.BenefitRequest;
import com.datlichkhambenh.backend.strategy.pricing.PricingAmounts;
import com.datlichkhambenh.backend.strategy.pricing.PricingChoice;
import com.datlichkhambenh.backend.strategy.pricing.PricingContext;
import com.datlichkhambenh.backend.strategy.service.BenefitService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * The one place that answers "can this patient book this slot, and at what price?" (see "Booking offer",
 * "Bookable slot" and "Covering benefit" in GLOSSARY.md). Pricing types, quotes and booking all go through it,
 * so the rules and the rejection codes cannot drift apart. Every refusal is a {@link BookingRejection}.
 *
 * <p>It decides; it does not save. Creating the appointment, closing the slot and publishing events stay with the
 * booking workflow.
 */
@Service
public class BookingOffer {
    static final String STANDARD = "STANDARD";
    private static final ZoneId CLINIC_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");

    private final ScheduleRepository slots;
    private final AppointmentRepository appointments;
    private final DoctorRepository doctors;
    private final PricingContext pricing;
    private final BenefitService benefits;

    public BookingOffer(ScheduleRepository slots, AppointmentRepository appointments, DoctorRepository doctors,
                        PricingContext pricing, BenefitService benefits) {
        this.slots = slots;
        this.appointments = appointments;
        this.doctors = doctors;
        this.pricing = pricing;
        this.benefits = benefits;
    }

    /** Pricing types this patient may pick for a bookable slot: Standard always, others with a covering benefit. */
    @Transactional(readOnly = true)
    public List<PricingChoice> pricingTypes(Long patientId, Long scheduleId) {
        Schedule slot = bookable(slots.findById(scheduleId).orElse(null), "Không thể báo giá ca khám đã qua");
        return pricing.choices().stream()
                .filter(c -> STANDARD.equals(c.key())
                        || benefits.covering(patientId, c.key(), slot.getWorkDate()).isPresent())
                .toList();
    }

    /** Booking offer for a bookable slot. {@code pricingKey} null means Standard. Nothing is locked. */
    @Transactional(readOnly = true)
    public Offer quote(Long patientId, Long scheduleId, String pricingKey) {
        Schedule slot = bookable(slots.findById(scheduleId).orElse(null), "Không thể báo giá ca khám đã qua");
        String key = supported(pricingKey == null ? STANDARD : pricingKey);
        if (!STANDARD.equals(key) && benefits.covering(patientId, key, slot.getWorkDate()).isEmpty()) {
            throw BookingRejection.BENEFIT_UNAVAILABLE.exception();
        }
        PricingAmounts amounts = pricing.calculate(key, slot.getDoctor().getConsultationFee());
        return new Offer(scheduleId, amounts, slot.getWorkDate(), slot.getStartTime(), slot.getEndTime());
    }

    /**
     * Locks and re-checks an offer against what the patient agreed to. Must run inside the caller's transaction;
     * locks are taken in this order and held until it ends: slot → doctor → benefit (only for non-Standard pricing).
     * Checks, in order: slot exists, is open, matches the agreed day/time (confirm route), has not started; doctor
     * accepts bookings; pricing type is given (confirm route) and supported; a covering benefit exists; the quote is
     * complete and its amounts are unchanged.
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public LockedOffer lockAndConfirm(Long patientId, Long scheduleId, Expectation expectation) {
        Schedule slot = slots.findByIdForUpdate(scheduleId).orElseThrow(BookingRejection.SLOT_MISSING::exception);
        requireOpen(slot);
        if (expectation instanceof Expectation.ConfirmedQuote quote) {
            requireAgreedSlotTime(slot, quote);
        }
        requireUpcoming(slot, "Không thể đặt ca khám đã qua");

        Doctor doctor = slot.getDoctor() == null ? null
                : doctors.findByIdForUpdate(slot.getDoctor().getUserId()).orElse(null);
        requireAcceptingDoctor(doctor);

        String key = supported(pricingKey(expectation));
        BenefitRequest benefit = null;
        if (!STANDARD.equals(key)) {
            benefit = benefits.lockCovering(patientId, key, slot.getWorkDate())
                    .orElseThrow(() -> BookingRejection.BENEFIT_UNAVAILABLE.exception(
                            "Quyền hưởng không còn hợp lệ cho ngày khám. Vui lòng chọn lại loại phí."));
        }
        PricingAmounts amounts = pricing.calculate(key, doctor.getConsultationFee());
        requireUnchangedPrice(expectation, amounts);
        return new LockedOffer(slot, doctor, benefit, amounts);
    }

    // ------------------------------------------------------------------ rules shared by the three operations

    private Schedule bookable(Schedule slot, String startedMessage) {
        if (slot == null) {
            throw BookingRejection.SLOT_MISSING.exception();
        }
        requireOpen(slot);
        requireUpcoming(slot, startedMessage);
        requireAcceptingDoctor(slot.getDoctor());
        return slot;
    }

    private void requireOpen(Schedule slot) {
        if (!Boolean.TRUE.equals(slot.getIsAvailable()) || appointments.existsByScheduleId(slot.getId())) {
            throw BookingRejection.SLOT_UNAVAILABLE.exception();
        }
    }

    private static void requireUpcoming(Schedule slot, String message) {
        if (!LocalDateTime.of(slot.getWorkDate(), slot.getStartTime()).isAfter(LocalDateTime.now(CLINIC_ZONE))) {
            throw BookingRejection.SLOT_UNAVAILABLE.exception(message);
        }
    }

    private static void requireAcceptingDoctor(Doctor doctor) {
        if (doctor == null || doctor.getUser() == null || !Boolean.TRUE.equals(doctor.getIsApproved())
                || !Boolean.TRUE.equals(doctor.getUser().getIsActive())) {
            throw BookingRejection.DOCTOR_UNAVAILABLE.exception();
        }
    }

    private static void requireAgreedSlotTime(Schedule slot, Expectation.ConfirmedQuote quote) {
        if (quote.workDate() == null || quote.startTime() == null || quote.endTime() == null) {
            throw BookingRejection.QUOTE_REQUIRED.exception("Cần ngày và giờ khám trong báo giá đã xác nhận");
        }
        if (!slot.getWorkDate().equals(quote.workDate()) || !slot.getStartTime().equals(quote.startTime())
                || !slot.getEndTime().equals(quote.endTime())) {
            throw BookingRejection.SLOT_UNAVAILABLE.exception(
                    "Ngày hoặc giờ của ca đã thay đổi. Vui lòng chọn lại ca và báo giá.");
        }
    }

    private String supported(String key) {
        if (!pricing.supports(key)) {
            throw BookingRejection.UNSUPPORTED_STRATEGY.exception();
        }
        return key;
    }

    private static String pricingKey(Expectation expectation) {
        return switch (expectation) {
            case Expectation.ConfirmedQuote q -> {
                if (q.pricingKey() == null) {
                    throw BookingRejection.QUOTE_REQUIRED.exception("Cần chọn loại phí và xác nhận báo giá");
                }
                yield q.pricingKey();
            }
            case Expectation.LegacyQuote q -> q.pricingKey() == null ? STANDARD : q.pricingKey();
            case Expectation.LegacyStandard s -> STANDARD;
        };
    }

    private static void requireUnchangedPrice(Expectation expectation, PricingAmounts amounts) {
        BigDecimal base;
        BigDecimal rate;
        BigDecimal price;
        switch (expectation) {
            case Expectation.ConfirmedQuote q -> {
                base = q.basePrice();
                rate = q.discountRate();
                price = q.finalPrice();
            }
            case Expectation.LegacyQuote q -> {
                base = q.basePrice();
                rate = q.discountRate();
                price = q.finalPrice();
            }
            case Expectation.LegacyStandard s -> {
                return;
            }
        }
        if (base == null || rate == null || price == null) {
            throw BookingRejection.QUOTE_REQUIRED.exception();
        }
        if (amounts.finalPrice().compareTo(price) != 0 || amounts.basePrice().compareTo(base) != 0
                || amounts.discountRate().compareTo(rate) != 0) {
            throw BookingRejection.PRICE_CHANGED.exception();
        }
    }
}
