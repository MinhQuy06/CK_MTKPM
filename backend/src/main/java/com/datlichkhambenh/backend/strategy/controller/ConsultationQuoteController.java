package com.datlichkhambenh.backend.strategy.controller;

import com.datlichkhambenh.backend.dto.response.ApiResponseDto;
import com.datlichkhambenh.backend.service.CurrentUserService;
import com.datlichkhambenh.backend.strategy.dto.request.ConsultationQuoteRequest;
import com.datlichkhambenh.backend.strategy.dto.response.ConsultationQuoteDto;
import com.datlichkhambenh.backend.strategy.offer.BookingOffer;
import com.datlichkhambenh.backend.strategy.offer.Offer;
import com.datlichkhambenh.backend.strategy.pricing.PricingChoice;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Pricing types and quotes for a slot: HTTP only; every rule lives in {@link BookingOffer}. */
@RestController
@RequestMapping("/api/v1/appointments")
@PreAuthorize("hasAuthority('PATIENT')")
public class ConsultationQuoteController {
    private final BookingOffer offers;
    private final CurrentUserService current;

    public ConsultationQuoteController(BookingOffer offers, CurrentUserService current) {
        this.offers = offers;
        this.current = current;
    }

    @GetMapping("/quote")
    public ResponseEntity<ApiResponseDto<ConsultationQuoteDto>> quote(@AuthenticationPrincipal UserDetails actor,
                                                                      @Valid @ModelAttribute ConsultationQuoteRequest request) {
        Offer offer = offers.quote(current.getUserId(actor), request.scheduleId(), request.strategyKey());
        var a = offer.amounts();
        var body = new ConsultationQuoteDto(offer.scheduleId(), a.strategyKey(), a.strategyName(), a.basePrice(),
                a.discountRate(), a.finalPrice(), "VND", false, offer.workDate(), offer.startTime(), offer.endTime());
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(ApiResponseDto.success(body));
    }

    @GetMapping("/pricing-options")
    public ResponseEntity<ApiResponseDto<List<PricingChoice>>> options(@AuthenticationPrincipal UserDetails actor,
                                                                       @Valid @ModelAttribute ConsultationQuoteRequest request) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(ApiResponseDto.success(offers.pricingTypes(current.getUserId(actor), request.scheduleId())));
    }
}
