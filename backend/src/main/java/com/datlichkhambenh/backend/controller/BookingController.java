package com.datlichkhambenh.backend.controller;

import com.datlichkhambenh.backend.dto.request.CreateBookingDto;
import com.datlichkhambenh.backend.dto.response.ApiResponseDto;
import com.datlichkhambenh.backend.dto.response.AppointmentDto;
import com.datlichkhambenh.backend.facade.BookingFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** HTTP only: authorisation, status codes and messages. Every booking action goes through {@link BookingFacade}. */
@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class BookingController {

    private final BookingFacade bookingFacade;

    @PostMapping
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<ApiResponseDto<AppointmentDto>> createBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CreateBookingDto request
    ) {
        AppointmentDto appointment = bookingFacade.bookWithoutQuote(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(appointment, "Đặt lịch khám thành công"));
    }

    @PostMapping("/confirm")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<ApiResponseDto<AppointmentDto>> confirmBooking(
            @AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody CreateBookingDto request) {
        AppointmentDto appointment = bookingFacade.confirm(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success(appointment, "Đặt lịch khám thành công"));
    }

    @DeleteMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<ApiResponseDto<Void>> cancelBooking(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long appointmentId
    ) {
        bookingFacade.cancel(userDetails, appointmentId);
        return ResponseEntity.ok(ApiResponseDto.success(null, "Hủy lịch hẹn thành công"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<ApiResponseDto<AppointmentDto>> getBookingDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable("id") Long appointmentId) {
        return ResponseEntity.ok(ApiResponseDto.success(bookingFacade.details(userDetails, appointmentId)));
    }

    @GetMapping("/my-appointments")
    @PreAuthorize("hasAuthority('PATIENT')")
    public ResponseEntity<ApiResponseDto<List<AppointmentDto>>> getMyPatientAppointments(@AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponseDto.success(bookingFacade.myAppointments(userDetails)));
    }
}
