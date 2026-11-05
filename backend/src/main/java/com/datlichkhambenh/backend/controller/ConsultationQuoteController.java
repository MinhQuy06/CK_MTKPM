package com.datlichkhambenh.backend.controller;

import com.datlichkhambenh.backend.dto.request.ConsultationQuoteRequest;
import com.datlichkhambenh.backend.dto.response.ApiResponseDto;
import com.datlichkhambenh.backend.dto.response.ConsultationQuoteDto;
import com.datlichkhambenh.backend.service.ConsultationQuoteService;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/appointments")
public class ConsultationQuoteController {
    private final ConsultationQuoteService service;

    public ConsultationQuoteController(ConsultationQuoteService service) {
        this.service = service;
    }

    @GetMapping("/quote")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<ApiResponseDto<ConsultationQuoteDto>> quote(
            @Valid @ModelAttribute ConsultationQuoteRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(ApiResponseDto.success(service.quote(request.scheduleId(), request.strategyKey())));
    }
}
