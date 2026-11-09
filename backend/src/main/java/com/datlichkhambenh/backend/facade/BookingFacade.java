package com.datlichkhambenh.backend.facade;

import com.datlichkhambenh.backend.dto.request.CreateBookingDto;
import com.datlichkhambenh.backend.dto.response.AppointmentDto;

/**
 * Facade Pattern: Cung cấp một giao diện đơn giản, thống nhất để điều phối
 * toàn bộ quy trình đặt lịch khám bệnh phức tạp giữa các hệ thống con (subsystems):
 * khóa slot (Pessimistic Lock), kiểm tra tính hợp lệ, lưu lịch hẹn, cập nhật slot,
 * vô hiệu hóa Redis cache và kích hoạt gửi email thông báo bất đồng bộ.
 */
public interface BookingFacade {

    /**
     * Điều phối toàn bộ quy trình đặt lịch khám cho bệnh nhân.
     *
     * @param patientUserId ID tài khoản bệnh nhân đặt lịch
     * @param request       DTO chứa mã ca khám (scheduleId) và triệu chứng ban đầu
     * @return AppointmentDto thông tin chi tiết lịch hẹn đã được xác nhận
     */
    AppointmentDto createBooking(Long patientUserId, CreateBookingDto request);
}

