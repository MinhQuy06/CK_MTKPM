package com.datlichkhambenh.backend.dto.request;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class UpdatePatientProfileDto {
    @Size(min = 2, max = 255, message = "Họ tên phải từ 2 đến 255 ký tự")
    private String fullName;

    @Past(message = "Ngày sinh phải ở trong quá khứ")
    private LocalDate dob;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Giới tính phải là MALE, FEMALE hoặc OTHER")
    private String gender;

    @Pattern(regexp = "^[0-9+]{9,15}$", message = "Số điện thoại không đúng định dạng")
    private String phone;

    @Size(max = 1000, message = "Địa chỉ quá dài")
    private String address;

    @Size(max = 5000, message = "Tiền sử bệnh quá dài")
    private String medicalHistory;
}
