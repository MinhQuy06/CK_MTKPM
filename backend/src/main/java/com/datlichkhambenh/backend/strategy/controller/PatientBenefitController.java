package com.datlichkhambenh.backend.strategy.controller;

import com.datlichkhambenh.backend.dto.response.ApiResponseDto;
import com.datlichkhambenh.backend.service.CurrentUserService;
import com.datlichkhambenh.backend.strategy.dto.request.SubmitBenefitRequest;
import com.datlichkhambenh.backend.strategy.dto.response.BenefitRequestDto;
import com.datlichkhambenh.backend.strategy.service.BenefitService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Bệnh nhân gửi và xem yêu cầu quyền hưởng của chính mình. Chỉ lo HTTP; nghiệp vụ ở BenefitService. */
@RestController
@RequestMapping("/api/v1/patients/me/benefit-requests")
@PreAuthorize("hasAuthority('PATIENT')")
public class PatientBenefitController {
    private final BenefitService benefits;
    private final CurrentUserService currentUser;

    public PatientBenefitController(BenefitService benefits, CurrentUserService currentUser) {
        this.benefits = benefits;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ResponseEntity<ApiResponseDto<BenefitRequestDto>> submit(@AuthenticationPrincipal UserDetails actor,
                                                                    @Valid @RequestBody SubmitBenefitRequest input) {
        BenefitRequestDto created = benefits.submit(currentUser.getUserId(actor), input);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseDto.success(created));
    }

    @GetMapping
    public ApiResponseDto<List<BenefitRequestDto>> mine(@AuthenticationPrincipal UserDetails actor) {
        return ApiResponseDto.success(benefits.mine(currentUser.getUserId(actor)));
    }

    @GetMapping("/{id}")
    public ApiResponseDto<BenefitRequestDto> detail(@AuthenticationPrincipal UserDetails actor, @PathVariable Long id) {
        return ApiResponseDto.success(benefits.detail(currentUser.getUserId(actor), id));
    }
}
