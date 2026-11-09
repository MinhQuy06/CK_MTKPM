package com.datlichkhambenh.backend.strategy.controller;

import com.datlichkhambenh.backend.strategy.dto.response.ConsultationQuoteDto;
import com.datlichkhambenh.backend.strategy.service.ConsultationQuoteService;

import com.datlichkhambenh.backend.strategy.dto.request.ConsultationQuoteRequest;
import com.datlichkhambenh.backend.dto.response.*;
import com.datlichkhambenh.backend.service.*;
import com.datlichkhambenh.backend.strategy.pricing.PricingChoice;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController @RequestMapping("/api/v1/appointments") @PreAuthorize("hasAuthority('PATIENT')")
public class ConsultationQuoteController {
 private final ConsultationQuoteService service;private final CurrentUserService current;
 public ConsultationQuoteController(ConsultationQuoteService service,CurrentUserService current){this.service=service;this.current=current;}
 @GetMapping("/quote")
 public ResponseEntity<ApiResponseDto<ConsultationQuoteDto>> quote(@AuthenticationPrincipal UserDetails actor,
  @Valid @ModelAttribute ConsultationQuoteRequest request) {
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ApiResponseDto.success(service.quote(current.getUserId(actor),request.scheduleId(),request.strategyKey())));
 }
 @GetMapping("/pricing-options")
 public ResponseEntity<ApiResponseDto<List<PricingChoice>>> options(@AuthenticationPrincipal UserDetails actor,
  @Valid @ModelAttribute ConsultationQuoteRequest request) {
  return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ApiResponseDto.success(service.options(current.getUserId(actor),request.scheduleId())));
 }
}
