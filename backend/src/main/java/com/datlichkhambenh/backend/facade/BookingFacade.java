package com.datlichkhambenh.backend.facade;

import com.datlichkhambenh.backend.dto.request.CreateBookingDto;
import com.datlichkhambenh.backend.dto.response.AppointmentDto;
import com.datlichkhambenh.backend.service.BookingService;
import com.datlichkhambenh.backend.service.CurrentUserService;
import com.datlichkhambenh.backend.strategy.offer.Expectation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Facade (GoF): one simple entry point for everything a signed-in user does with appointments.
 * Behind it sit the subsystems the controller no longer needs to know: who the user is
 * ({@link CurrentUserService}) and the booking workflow ({@link BookingService}), which in turn locks
 * the slot, checks benefits, prices with Strategy and notifies through Observer/Decorator.
 */
@Component
@RequiredArgsConstructor
public class BookingFacade {
    private final CurrentUserService currentUser;
    private final BookingService bookings;

    /**
     * New UI flow: the patient sends back the full agreed quote, slot day and times included. Missing parts are
     * refused as QUOTE_REQUIRED; this route can never fall back to a legacy booking.
     */
    public AppointmentDto confirm(UserDetails patient, CreateBookingDto request) {
        Expectation agreed = new Expectation.ConfirmedQuote(request.getStrategyKey(), request.getExpectedBasePrice(),
                request.getExpectedDiscountRate(), request.getExpectedPrice(), request.getExpectedWorkDate(),
                request.getExpectedStartTime(), request.getExpectedEndTime());
        return book(patient, request, agreed);
    }

    /**
     * Older clients: without any quote field it is a legacy booking (Standard only); with a pricing type or prices
     * the amounts are still checked, but not the slot's day and times.
     */
    public AppointmentDto bookWithoutQuote(UserDetails patient, CreateBookingDto request) {
        boolean carriesQuote = request.getStrategyKey() != null || request.getExpectedPrice() != null
                || request.getExpectedBasePrice() != null || request.getExpectedDiscountRate() != null;
        Expectation expectation = carriesQuote
                ? new Expectation.LegacyQuote(request.getStrategyKey(), request.getExpectedBasePrice(),
                        request.getExpectedDiscountRate(), request.getExpectedPrice())
                : new Expectation.LegacyStandard();
        return book(patient, request, expectation);
    }

    private AppointmentDto book(UserDetails patient, CreateBookingDto request, Expectation expectation) {
        return bookings.createBooking(currentUser.getUserId(patient), request.getScheduleId(), request.getSymptoms(),
                expectation);
    }

    public void cancel(UserDetails patient, Long appointmentId) {
        bookings.cancelBooking(appointmentId, currentUser.getUserId(patient));
    }

    /** Owner patient, the appointment's doctor or an Admin; the role comes from the signed-in user. */
    public AppointmentDto details(UserDetails requester, Long appointmentId) {
        String role = requester.getAuthorities().iterator().next().getAuthority();
        return bookings.getBookingDetails(appointmentId, currentUser.getUserId(requester), role);
    }

    public List<AppointmentDto> myAppointments(UserDetails patient) {
        return bookings.getPatientAppointments(currentUser.getUserId(patient));
    }
}
