package com.datlichkhambenh.backend.facade;

import com.datlichkhambenh.backend.dto.request.CreateBookingDto;
import com.datlichkhambenh.backend.dto.response.AppointmentDto;
import com.datlichkhambenh.backend.service.BookingService;
import com.datlichkhambenh.backend.service.CurrentUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

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

    /** New UI flow: the patient must send back the quote they agreed to. */
    public AppointmentDto confirm(UserDetails patient, CreateBookingDto request) {
        request.setQuoteRequired(true);
        return bookings.createBooking(currentUser.getUserId(patient), request);
    }

    /** Older clients without a quote: Standard price only. */
    public AppointmentDto bookWithoutQuote(UserDetails patient, CreateBookingDto request) {
        return bookings.createBooking(currentUser.getUserId(patient), request);
    }
}
