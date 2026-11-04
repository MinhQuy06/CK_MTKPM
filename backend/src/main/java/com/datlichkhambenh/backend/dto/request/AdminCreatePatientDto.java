package com.datlichkhambenh.backend.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class AdminCreatePatientDto {
    @NotBlank @Email private String email;
    @NotBlank @Size(min = 8, message = "Mật khẩu phải từ 8 ký tự") private String password;
    @NotBlank private String fullName;
    @Pattern(regexp = "^[0-9+]{9,15}$", message = "Số điện thoại không đúng định dạng") private String phone;
    @Past private LocalDate dob;
    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$") private String gender;
    private String address;
    private String medicalHistory;
}
