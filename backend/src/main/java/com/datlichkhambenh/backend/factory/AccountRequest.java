package com.datlichkhambenh.backend.factory;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Mọi thông tin cần để mở tài khoản, dù ai yêu cầu (tự đăng ký hay Admin tạo).
 * Factory bệnh nhân bỏ qua các trường của bác sĩ và ngược lại.
 *
 * @param email    đã chuẩn hóa (bỏ khoảng trắng, chữ thường)
 * @param approved chỉ cho bác sĩ: có được nhận lịch ngay không
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
