package com.datlichkhambenh.backend.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class AdminCreateDoctorDto {
    @NotBlank @Email private String email;
    @NotBlank @Size(min = 8, message = "Mật khẩu phải từ 8 ký tự") private String password;
    @NotBlank private String fullName;
    @Pattern(regexp = "^[0-9+]{9,15}$", message = "Số điện thoại không đúng định dạng") private String phone;
    @NotNull private Long specialtyId;
    private String degree;
    @PositiveOrZero private Integer experienceYears;
    @DecimalMin("0") private BigDecimal consultationFee;
    private Boolean approved;
}
