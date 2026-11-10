package com.datlichkhambenh.backend.strategy.offer;

import com.datlichkhambenh.backend.exception.BookingRejectionException;
import com.datlichkhambenh.backend.support.BookingApiFixture;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** The booking offer module through its own interface (Spring + H2, no mocks), plus the two unified HTTP codes. */
class BookingOfferTests extends BookingApiFixture {
    @Autowired BookingOffer offers;

    private static final BigDecimal FEE = new BigDecimal("300000");

    void rejected(String code, int status, Executable call) {
        var error = assertThrows(BookingRejectionException.class, call);
        assertEquals(code, error.getCode());
        assertEquals(status, error.getStatus().value());
    }

    <T> T inTransaction(Supplier<T> work) {
        return new TransactionTemplate(tm).execute(tx -> work.get());
    }

    Expectation.ConfirmedQuote agreed(BigDecimal price, LocalTime start) {
        return new Expectation.ConfirmedQuote("STANDARD", FEE, BigDecimal.ZERO, price, day, start, start.plusMinutes(30));
    }

    void deactivateDoctor() {
        new TransactionTemplate(tm).executeWithoutResult(tx -> {
            var doctor = doctors.findById(doctorId).orElseThrow();
            doctor.setIsApproved(false);
            doctors.save(doctor);
        });
    }

    // ------------------------------------------------------------------ quote and pricing types

    @Test
    void standardQuoteForABookableSlot() {
        var offer = offers.quote(patientId, scheduleId, null);
        assertEquals("STANDARD", offer.amounts().strategyKey());
        assertEquals(0, offer.amounts().finalPrice().compareTo(FEE));
        assertEquals(day, offer.workDate());
    }

    @Test
    void pricingTypesWithoutBenefitAreStandardOnly() {
        var keys = offers.pricingTypes(patientId, scheduleId).stream().map(c -> c.key()).toList();
        assertEquals(java.util.List.of("STANDARD"), keys);
    }

    @Test
    void quoteRejectsMissingSlot() {
        rejected("SLOT_UNAVAILABLE", 404, () -> offers.quote(patientId, -1L, null));
    }

    @Test
    void quoteRejectsTakenSlot() throws Exception {
        book();
        rejected("SLOT_UNAVAILABLE", 409, () -> offers.quote(patientId, scheduleId, null));
    }

    @Test
    void quoteRejectsUnsupportedPricingTypeWithCode() {
        rejected("UNSUPPORTED_STRATEGY", 400, () -> offers.quote(patientId, scheduleId, "GOLD"));
    }

    @Test
    void quoteRejectsDoctorNotAcceptingWithCode() {
        deactivateDoctor();
        rejected("DOCTOR_UNAVAILABLE", 409, () -> offers.quote(patientId, scheduleId, null));
    }

    @Test
    void quoteRejectsInsuranceWithoutCoveringBenefit() {
        rejected("BENEFIT_UNAVAILABLE", 403, () -> offers.quote(patientId, scheduleId, "INSURANCE"));
    }

    // ------------------------------------------------------------------ lock and confirm

    @Test
    void lockAndConfirmRefusesToRunOutsideATransaction() {
        assertThrows(IllegalTransactionStateException.class,
                () -> offers.lockAndConfirm(patientId, scheduleId, new Expectation.LegacyStandard()));
    }

    @Test
    void matchingAgreedQuoteIsLockedWithoutBenefit() {
        var locked = inTransaction(() -> offers.lockAndConfirm(patientId, scheduleId, agreed(FEE, LocalTime.of(9, 0))));
        assertEquals(scheduleId, locked.slot().getId());
        assertEquals(doctorId, locked.doctor().getUserId());
        assertNull(locked.benefit());
        assertEquals(0, locked.amounts().finalPrice().compareTo(FEE));
    }

    @Test
    void changedPriceIsRejected() {
        rejected("PRICE_CHANGED", 409,
                () -> inTransaction(() -> offers.lockAndConfirm(patientId, scheduleId, agreed(BigDecimal.ONE, LocalTime.of(9, 0)))));
    }

    @Test
    void changedSlotTimeIsRejected() {
        rejected("SLOT_UNAVAILABLE", 409,
                () -> inTransaction(() -> offers.lockAndConfirm(patientId, scheduleId, agreed(FEE, LocalTime.of(10, 0)))));
    }

    @Test
    void agreedQuoteWithoutSlotTimeIsIncomplete() {
        var incomplete = new Expectation.ConfirmedQuote("STANDARD", FEE, BigDecimal.ZERO, FEE, null, null, null);
        rejected("QUOTE_REQUIRED", 400, () -> inTransaction(() -> offers.lockAndConfirm(patientId, scheduleId, incomplete)));
    }

    @Test
    void legacyBookingIsStandard() {
        var locked = inTransaction(() -> offers.lockAndConfirm(patientId, scheduleId, new Expectation.LegacyStandard()));
        assertEquals("STANDARD", locked.amounts().strategyKey());
    }

    @Test
    void legacyQuoteForInsuranceStillNeedsACoveringBenefit() {
        var legacy = new Expectation.LegacyQuote("INSURANCE", FEE, new BigDecimal("0.8"), new BigDecimal("60000"));
        rejected("BENEFIT_UNAVAILABLE", 403, () -> inTransaction(() -> offers.lockAndConfirm(patientId, scheduleId, legacy)));
    }

    @Test
    void lockAndConfirmRejectsDoctorNotAcceptingWithCode() {
        deactivateDoctor();
        rejected("DOCTOR_UNAVAILABLE", 409,
                () -> inTransaction(() -> offers.lockAndConfirm(patientId, scheduleId, new Expectation.LegacyStandard())));
    }

    // ------------------------------------------------------------------ the two codes unified across HTTP routes

    @Test
    void quoteEndpointNowReturnsUnsupportedStrategyCode() throws Exception {
        String path = PATH + "/quote";
        mvc.perform(get(path).servletPath(path).header("Authorization", "Bearer " + patientToken)
                        .param("scheduleId", scheduleId.toString()).param("strategyKey", "GOLD"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("UNSUPPORTED_STRATEGY"));
    }

    @Test
    void bookingEndpointNowReturnsDoctorUnavailableCode() throws Exception {
        deactivateDoctor();
        var response = confirm(confirmBody());
        assertEquals(409, response.getStatus());
        assertEquals("DOCTOR_UNAVAILABLE", code(response));
    }
}
