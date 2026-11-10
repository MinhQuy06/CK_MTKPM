package com.datlichkhambenh.backend.factory;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Everything needed to open a clinic account, whoever asks for it (self-registration or Admin).
 * Doctor-only fields are ignored by the patient factory and vice versa.
 *
 * @param email    already normalised (trimmed, lower case)
 * @param approved doctor only: whether the doctor may take bookings right away
 */
public record AccountRequest(
        String email,
        String rawPassword,
        String fullName,
        String phone,
        LocalDate dob,
        String gender,
        String address,
        String medicalHistory,
        Long specialtyId,
        String degree,
        Integer experienceYears,
        BigDecimal consultationFee,
        boolean approved
) {
}
