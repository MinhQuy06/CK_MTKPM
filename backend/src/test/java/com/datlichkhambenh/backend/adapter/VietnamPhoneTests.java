package com.datlichkhambenh.backend.adapter;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Adapter ticket 11: local Vietnamese numbers become the vendor's international form. */
class VietnamPhoneTests {

    @ParameterizedTest
    @CsvSource({
            "0912345678, +84912345678",
            "0912 345 678, +84912345678",
            "0912.345.678, +84912345678",
            "0912-345-678, +84912345678",
            "84912345678, +84912345678",
            "+84912345678, +84912345678",
            "+84 912 345 678, +84912345678"
    })
    void commonFormatsBecomeInternational(String stored, String expected) {
        assertEquals(Optional.of(expected), VietnamPhone.toInternational(stored));
    }

    @ParameterizedTest
    @ValueSource(strings = {"12345", "091234567", "09123456789", "0912abc678", "+1 415 555 0100", "0012345678"})
    void malformedNumbersAreRejected(String stored) {
        assertEquals(Optional.empty(), VietnamPhone.toInternational(stored));
    }
}
