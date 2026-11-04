package com.datlichkhambenh.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateDoctorProfileDto {
    @Size(min = 2, max = 255, message = "Họ tên phải từ 2 đến 255 ký tự")
    private String fullName;

    @Pattern(regexp = "^[0-9+]{9,15}$", message = "Số điện thoại không đúng định dạng")
    private String phone;

    @Size(max = 100, message = "Bằng cấp quá dài")
    private String degree;

    @PositiveOrZero(message = "Số năm kinh nghiệm không được âm")
    private Integer experienceYears;

    @DecimalMin(value = "0", message = "Giá khám không được âm")
    private BigDecimal consultationFee;

    private Long specialtyId;
}
