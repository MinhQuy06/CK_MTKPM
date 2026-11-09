package com.datlichkhambenh.backend.strategy.pricing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PricingContextTests {
    @Test
    void registeredStrategyIsExecutedWithoutChangingContext() {
        ConsultationPricingStrategy custom = new ConsultationPricingStrategy() {
            public String key() { return "CUSTOM"; }
            public String name() { return "Custom test algorithm"; }
            public BigDecimal discountRate() { return new BigDecimal("0.5"); }
            public BigDecimal calculate(BigDecimal basePrice) { return basePrice.subtract(new BigDecimal("177")); }
            public boolean simulated() { return true; }
        };
        PricingContext context = new PricingContext(List.of(new StandardPricingStrategy(BigDecimal.ZERO), custom));
        PricingAmounts result = context.calculate("CUSTOM", new BigDecimal("300"));
        assertEquals(new BigDecimal("123"), result.finalPrice());
        assertEquals("CUSTOM", result.strategyKey());
        assertTrue(result.simulated());
        assertEquals(new BigDecimal("300"), context.calculate(null, new BigDecimal("300")).finalPrice());
    }

    @Test
    void standardRateIsConfigurableAndAmountsRoundHalfUp() {
        PricingContext context = new PricingContext(List.of(new StandardPricingStrategy(new BigDecimal("0.1"))));
        PricingAmounts result = context.calculate("STANDARD", new BigDecimal("100.50"));
        assertEquals(new BigDecimal("101"), result.basePrice());
        assertEquals(new BigDecimal("91"), result.finalPrice());
        assertEquals(new BigDecimal("0.1"), result.discountRate());
    }

    @ParameterizedTest
    @ValueSource(strings = {"-0.001", "1.001"})
    void invalidConfiguredDiscountIsRejected(String rate) {
        assertThrows(IllegalArgumentException.class, () -> new StandardPricingStrategy(new BigDecimal(rate)));
    }

    @Test
    void nullDiscountIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> new StandardPricingStrategy(null));
    }

    @Test
    void zeroAndFullDiscountAreValid() {
        assertEquals(new BigDecimal("0"), new PricingContext(List.of(new StandardPricingStrategy(BigDecimal.ONE)))
                .calculate(null, new BigDecimal("300000")).finalPrice());
    }

    @Test
    void duplicateKeysFailRatherThanSilentlySelectOneStrategy() {
        assertThrows(IllegalArgumentException.class, () -> new PricingContext(List.of(
                new StandardPricingStrategy(BigDecimal.ZERO), new StandardPricingStrategy(BigDecimal.ONE))));
    }

    @Test
    void feesOutsideStorageCapacityAreRejectedBeforeRounding() {
        PricingContext context = new PricingContext(List.of(new StandardPricingStrategy(BigDecimal.ZERO)));
        assertThrows(IllegalArgumentException.class, () -> context.calculate(null, new BigDecimal("10000000000")));
        assertThrows(IllegalArgumentException.class, () -> context.calculate(null, new BigDecimal("9999999999.50")));
        assertThrows(IllegalArgumentException.class, () -> context.calculate(null, new BigDecimal("-0.001")));
    }
}
