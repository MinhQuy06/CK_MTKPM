package com.datlichkhambenh.backend.controller;

import com.datlichkhambenh.backend.dto.request.MedicalServiceRequestDto;
import com.datlichkhambenh.backend.dto.response.ApiResponseDto;
import com.datlichkhambenh.backend.dto.response.MedicalServiceDto;
import com.datlichkhambenh.backend.service.MedicalServiceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/services")
@RequiredArgsConstructor
public class MedicalServiceController {
    private final MedicalServiceService medicalServiceService;

    @GetMapping
    public ApiResponseDto<List<MedicalServiceDto>> list() {
        return ApiResponseDto.success(medicalServiceService.getActiveServices());
    }

    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponseDto<List<MedicalServiceDto>> listAdmin() {
        return ApiResponseDto.success(medicalServiceService.getAllServices());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponseDto<MedicalServiceDto> create(@Valid @RequestBody MedicalServiceRequestDto request) {
        return ApiResponseDto.success(medicalServiceService.create(request), "Đã thêm dịch vụ");
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponseDto<MedicalServiceDto> update(@PathVariable Long id,
                                                     @Valid @RequestBody MedicalServiceRequestDto request) {
        return ApiResponseDto.success(medicalServiceService.update(id, request), "Đã cập nhật dịch vụ");
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ApiResponseDto<Void> disable(@PathVariable Long id) {
        medicalServiceService.disable(id);
        return ApiResponseDto.success(null, "Đã ngừng cung cấp dịch vụ");
    }
}
