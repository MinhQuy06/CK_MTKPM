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
 * Facade (GoF): một cửa vào cho mọi thao tác lịch hẹn của người đã đăng nhập. Phía sau là các hệ thống con
 * mà controller không cần biết: ai đang đăng nhập ({@link CurrentUserService}) và quy trình đặt lịch
 * ({@link BookingService}), bên trong đó khóa ca, kiểm quyền hưởng, tính giá bằng Strategy và thông báo
 * qua Observer / Decorator.
 */
@Component
@RequiredArgsConstructor
public class BookingFacade {
    private final CurrentUserService currentUser;
    private final BookingService bookings;

    /**
     * Luồng giao diện mới: bệnh nhân gửi lại đủ giá đã đồng ý, kể cả ngày giờ ca. Thiếu phần nào thì bị từ chối
     * với QUOTE_REQUIRED; route này không bao giờ hạ cấp xuống đặt lịch kiểu cũ.
     */
    public AppointmentDto confirm(UserDetails patient, CreateBookingDto request) {
        Expectation agreed = new Expectation.ConfirmedQuote(request.getStrategyKey(), request.getExpectedBasePrice(),
                request.getExpectedDiscountRate(), request.getExpectedPrice(), request.getExpectedWorkDate(),
                request.getExpectedStartTime(), request.getExpectedEndTime());
        return book(patient, request, agreed);
    }

    /**
     * Client cũ: không kèm trường báo giá nào thì là đặt lịch kiểu cũ (chỉ Tiêu chuẩn); kèm loại phí hoặc giá thì
     * số tiền vẫn được so, nhưng ngày giờ ca thì không.
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

    /** Chủ lịch, bác sĩ của lịch hoặc Admin được xem; vai trò lấy từ người đang đăng nhập. */
    public AppointmentDto details(UserDetails requester, Long appointmentId) {
        String role = requester.getAuthorities().iterator().next().getAuthority();
        return bookings.getBookingDetails(appointmentId, currentUser.getUserId(requester), role);
    }

    public List<AppointmentDto> myAppointments(UserDetails patient) {
        return bookings.getPatientAppointments(currentUser.getUserId(patient));
    }
}
