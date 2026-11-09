package com.datlichkhambenh.backend.strategy.controller;

import com.datlichkhambenh.backend.strategy.dto.response.BenefitRequestDto;

import com.datlichkhambenh.backend.strategy.dto.request.SubmitBenefitRequest;
import com.datlichkhambenh.backend.dto.response.*;
import com.datlichkhambenh.backend.strategy.service.BenefitService;
import com.datlichkhambenh.backend.service.CurrentUserService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/v1/patients/me/benefit-requests")
@PreAuthorize("hasAuthority('PATIENT')")
public class PatientBenefitController {
    private final BenefitService benefits;
    private final CurrentUserService currentUser;
    public PatientBenefitController(BenefitService benefits,CurrentUserService currentUser) {
        this.benefits=benefits;this.currentUser=currentUser;
    }
    @PostMapping
    public ResponseEntity<ApiResponseDto<BenefitRequestDto>> submit(@AuthenticationPrincipal UserDetails actor,
            @Valid @RequestBody SubmitBenefitRequest input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(benefits.submit(currentUser.getUserId(actor),input)));
    }
    @GetMapping
    public ApiResponseDto<List<BenefitRequestDto>> mine(@AuthenticationPrincipal UserDetails actor) {
        return ApiResponseDto.success(benefits.mine(currentUser.getUserId(actor)));
    }
    @GetMapping("/{id}")
    public ApiResponseDto<BenefitRequestDto> detail(@AuthenticationPrincipal UserDetails actor,@PathVariable Long id) {
        return ApiResponseDto.success(benefits.detail(currentUser.getUserId(actor),id));
    }
}
