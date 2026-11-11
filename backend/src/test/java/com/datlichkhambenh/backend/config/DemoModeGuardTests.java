package com.datlichkhambenh.backend.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DemoModeGuardTests {
    @Test
    void allowsLocalMailpitWithSmsOff() {
        assertDoesNotThrow(() -> new DemoModeGuard("mailpit", false).afterPropertiesSet());
        assertDoesNotThrow(() -> new DemoModeGuard("localhost", false).afterPropertiesSet());
    }

    @Test
    void refusesRealSmtpInDemo() {
        assertThrows(IllegalStateException.class, () -> new DemoModeGuard("smtp.gmail.com", false).afterPropertiesSet());
    }

    @Test
    void refusesRealSmsInDemo() {
        assertThrows(IllegalStateException.class, () -> new DemoModeGuard("mailpit", true).afterPropertiesSet());
    }
}
