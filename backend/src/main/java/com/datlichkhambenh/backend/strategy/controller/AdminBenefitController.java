package com.datlichkhambenh.backend.strategy.controller;

import com.datlichkhambenh.backend.dto.response.ApiResponseDto;
import com.datlichkhambenh.backend.service.CurrentUserService;
import com.datlichkhambenh.backend.strategy.dto.request.BenefitDecisionInput;
import com.datlichkhambenh.backend.strategy.dto.request.BenefitRevokeInput;
import com.datlichkhambenh.backend.strategy.dto.response.BenefitRequestDto;
import com.datlichkhambenh.backend.strategy.service.BenefitService;
import jakarta.validation.Valid;
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

/** Admin xem, duyệt / từ chối và thu hồi yêu cầu quyền hưởng. Chỉ lo HTTP; nghiệp vụ ở BenefitService. */
@RestController
@RequestMapping("/api/v1/admin/benefit-requests")
@PreAuthorize("hasAuthority('ADMIN')")
public class AdminBenefitController {
    private final BenefitService benefits;
    private final CurrentUserService current;

    public AdminBenefitController(BenefitService benefits, CurrentUserService current) {
        this.benefits = benefits;
        this.current = current;
    }

    @GetMapping
    public ApiResponseDto<List<BenefitRequestDto>> all() {
        return ApiResponseDto.success(benefits.all());
    }

    @GetMapping("/{id}")
    public ApiResponseDto<BenefitRequestDto> detail(@PathVariable Long id) {
        return ApiResponseDto.success(benefits.adminDetail(id));
    }

    @PostMapping("/{id}/decision")
    public ApiResponseDto<BenefitRequestDto> decide(@AuthenticationPrincipal UserDetails actor, @PathVariable Long id,
                                                    @Valid @RequestBody BenefitDecisionInput input) {
        return ApiResponseDto.success(benefits.decide(id, current.getUserId(actor), input));
    }

    @PostMapping("/{id}/revoke")
    public ApiResponseDto<BenefitRequestDto> revoke(@AuthenticationPrincipal UserDetails actor, @PathVariable Long id,
                                                    @Valid @RequestBody BenefitRevokeInput input) {
        return ApiResponseDto.success(benefits.revoke(id, current.getUserId(actor), input));
    }
}
