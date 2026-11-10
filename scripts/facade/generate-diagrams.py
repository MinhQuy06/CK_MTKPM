#!/usr/bin/env python3
"""Facade pattern — class, sequence, ERD and DFD (editable, black-and-white draw.io)."""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'common'))
from drawio_kit import (Diagram, Sequence, REPO, DEPENDS, EXTERNAL, PROCESS, STORE)  # noqa: E402

OUT = REPO / 'docs/facade/diagrams'


def class_diagram():
    d = Diagram('Facade — Class', 2200, 1160, OUT)
    d.title('MẪU FACADE — MỘT CỬA CHO THAO TÁC LỊCH HẸN — SƠ ĐỒ LỚP', 2100)
    d.uml('controller', '«Client · REST»\nBookingController', [
        '- bookingFacade: BookingFacade   (phụ thuộc duy nhất)', '',
        '+ createBooking   POST /appointments', '+ confirmBooking  POST /appointments/confirm',
        '+ cancelBooking   DELETE /appointments/{id}/cancel', '+ getBookingDetails  GET /appointments/{id}',
        '+ getMyPatientAppointments  GET /appointments/my-appointments', '',
        'Chỉ lo HTTP: @PreAuthorize, mã trạng thái, thông điệp'], 40, 110, 620)
    d.uml('facade', '«Facade»\nBookingFacade', [
        '- currentUser: CurrentUserService', '- bookings: BookingService', '',
        '+ confirm(user, request): AppointmentDto   // bắt buộc báo giá',
        '+ bookWithoutQuote(user, request): AppointmentDto',
        '+ cancel(user, appointmentId)', '+ details(user, appointmentId): AppointmentDto  // lấy vai trò từ user',
        '+ myAppointments(user): List<AppointmentDto>'], 800, 110, 640)
    d.box('subsystem', '', 760, 560, 1400, 470, 'fillColor=none;dashed=1;')
    d.text('subsystem-label', 'Hệ thống con (controller không còn biết tới)', 780, 565, 800, 30, 16)
    d.uml('current', '«subsystem»\nCurrentUserService', ['+ getUserId(userDetails): Long'], 800, 610, 520)
    d.uml('booking', '«subsystem»\nBookingService (BookingServiceImpl)', [
        '+ createBooking(patientId, request)', '+ cancelBooking(id, patientId)',
        '+ getBookingDetails(id, requesterId, role)', '+ getPatientAppointments(patientId)'], 1420, 610, 700)
    d.uml('deep', '«subsystem · bên trong đặt lịch»\nRepositories · PricingContext · BenefitService · ApplicationEventPublisher',
          ['Khóa ca và bác sĩ, kiểm tra quyền hưởng, tính giá (Strategy),', 'lưu lịch hẹn, phát sự kiện (Observer → Decorator)'],
          1420, 860, 700)
    d.uml('strategyctl', '«không qua Facade»\nConsultationQuoteController · Patient/AdminBenefitController',
          ['Endpoint Strategy giữ nguyên'], 40, 610, 620)
    d.edge('controller', 'facade', 'mọi thao tác lịch hẹn', 'exitX=1;exitY=0.3;entryX=0;entryY=0.3;')
    d.edge('facade', 'current', 'ai đang đăng nhập', 'exitX=0.3;exitY=1;entryX=0.5;entryY=0;')
    d.edge('facade', 'booking', 'quy trình đặt lịch', 'exitX=0.8;exitY=1;entryX=0.3;entryY=0;')
    d.edge('booking', 'deep', 'điều phối', DEPENDS + 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
    d.text('note', 'Trước: controller tự lấy userId, tự đọc vai trò, tự bật quoteRequired và gọi thẳng BookingService. '
           'Sau: controller chỉ gọi BookingFacade; đổi cách xác định người dùng hay quy trình đặt lịch không chạm tới '
           'controller.\nFacade không thay thế các hệ thống con: code khác vẫn có thể gọi BookingService trực tiếp.',
           40, 1060, 2100, 70, 15)
    d.save('facade-class.drawio')


def sequence():
    s = Sequence('Facade — Sequence: xác nhận đặt lịch qua một cửa', [
        'Bệnh nhân (UI)', 'BookingController', 'BookingFacade', 'CurrentUserService', 'BookingService',
        'Hệ thống con bên trong\n(Strategy, Observer, …)'], OUT, col=320)
    s.call(0, 1, 'POST /appointments/confirm  (JWT PATIENT, báo giá đã đồng ý)')
    s.remark('@PreAuthorize("hasAuthority(\'PATIENT\')") + @Valid CreateBookingDto — vẫn nằm ở controller.')
    s.call(1, 2, 'confirm(userDetails, request)')
    s.step(2, 'request.setQuoteRequired(true)', 300, 44)
    s.call(2, 3, 'getUserId(userDetails)')
    s.reply(3, 2, 'patientId')
    s.call(2, 4, 'createBooking(patientId, request)')
    s.call(4, 5, 'khóa ca → kiểm tra quyền hưởng → tính giá → lưu → phát sự kiện')
    s.reply(5, 4, 'Appointment đã lưu')
    s.begin('alt [ca kín / giá đổi / mất quyền hưởng]')
    s.reply(4, 1, 'BookingRejectionException → 409 / 403 (đi xuyên Facade)')
    s.end()
    s.reply(4, 2, 'AppointmentDto')
    s.reply(2, 1, 'AppointmentDto')
    s.reply(1, 0, '201 "Đặt lịch khám thành công"')
    s.footer('Hủy, xem chi tiết và danh sách lịch của tôi đi cùng kiểu: controller → một hàm của Facade → '
             'CurrentUserService + BookingService. details() lấy vai trò từ người đăng nhập để kiểm tra quyền xem.')
    s.save('facade-sequence.drawio')


def erd():
    d = Diagram('Facade — ERD', 2000, 900, OUT)
    d.title('MẪU FACADE — ERD: DỮ LIỆU PHÍA SAU CỬA ĐẶT LỊCH', 1900)
    d.table('users', 'users  (CurrentUserService đọc)', [('PK', 'id', 'BIGINT'), ('', 'email', 'VARCHAR(255) UK'),
                                                           ('', 'role', 'VARCHAR(20)'), ('', '…', '')], 60, 120, 500)
    d.table('patients', 'patients', [('PK,FK', 'user_id', 'BIGINT'), ('', 'full_name', 'VARCHAR(255)'), ('', '…', '')],
            60, 480, 500)
    d.table('appointments', 'appointments', [
        ('PK', 'id', 'BIGINT'), ('', 'booking_code', 'VARCHAR(100) UK'), ('FK', 'patient_id', 'BIGINT'),
        ('FK', 'doctor_id', 'BIGINT'), ('FK', 'schedule_id', 'BIGINT UK'), ('', 'status', 'VARCHAR(20)'),
        ('', 'price · pricing_key · base_price', ''), ('', 'discount_rate · benefit_request_id', '')], 720, 300, 580)
    d.table('doctors', 'doctors', [('PK,FK', 'user_id', 'BIGINT'), ('', 'consultation_fee', 'DECIMAL(12,2)'),
                                   ('', '…', '')], 1440, 120, 480)
    d.table('schedules', 'schedules', [('PK', 'id', 'BIGINT'), ('FK', 'doctor_id', 'BIGINT'), ('', 'work_date', 'DATE'),
                                       ('', 'is_available', 'BOOLEAN')], 1440, 480, 480)
    d.rel('patients', 'users', 'user_id (1–1)', 'ERzeroToOne', 'ERmandOne', 'exitX=0.5;exitY=0;entryX=0.5;entryY=1;')
    d.rel('appointments', 'patients', 'patient_id', 'ERzeroToMany', 'ERmandOne', 'exitX=0;exitY=0.7;entryX=1;entryY=0.4;')
    d.rel('appointments', 'doctors', 'doctor_id', 'ERzeroToMany', 'ERmandOne', 'exitX=1;exitY=0.2;entryX=0;entryY=0.5;')
    d.rel('appointments', 'schedules', 'schedule_id', 'ERzeroToOne', 'ERmandOne', 'exitX=1;exitY=0.7;entryX=0;entryY=0.5;')
    d.rel('schedules', 'doctors', 'doctor_id', 'ERzeroToMany', 'ERmandOne', 'exitX=0.5;exitY=0;entryX=0.5;entryY=1;')
    d.text('legend', 'Facade không thêm bảng và không truy cập DB trực tiếp: mọi đọc/ghi đi qua hệ thống con '
           '(CurrentUserService, BookingService). Sơ đồ cho thấy dữ liệu nằm phía sau cửa đặt lịch.\n'
           'Ký hiệu crow\'s foot: ┤├ đúng một · o┤ không hoặc một · o< không hoặc nhiều.', 60, 790, 1880, 70, 15)
    d.save('facade-erd.drawio')


def dfd():
    d = Diagram('Facade — DFD', 2100, 900, OUT)
    d.title('MẪU FACADE — DFD MỨC 1: THAO TÁC LỊCH HẸN QUA MỘT CỬA', 2000)
    d.box('patient', 'Bệnh nhân', 40, 200, 220, 120, EXTERNAL)
    d.box('staff', 'Bác sĩ / Admin\n(xem chi tiết)', 40, 470, 220, 120, EXTERNAL)
    d.box('p1', '1.0\nCửa đặt lịch\n(BookingFacade)', 400, 290, 240, 200, PROCESS)
    d.box('p2', '2.0\nXác định người\nđăng nhập', 860, 120, 220, 190, PROCESS)
    d.box('p3', '3.0\nQuy trình\nđặt / hủy / xem', 860, 460, 220, 190, PROCESS)
    d.box('d1', 'D1   users', 1320, 190, 260, 56, STORE)
    d.box('d2', 'D2   appointments · schedules · doctors', 1320, 530, 460, 56, STORE)
    d.edge('patient', 'p1', 'đặt / hủy / danh sách', 'exitX=1;exitY=0.8;entryX=0.05;entryY=0.35;')
    d.edge('p1', 'patient', 'lịch hẹn / lỗi 4xx', 'exitX=0.3;exitY=0;entryX=1;entryY=0.25;', [(472, 230)])
    d.edge('staff', 'p1', 'xem chi tiết', 'exitX=1;exitY=0.5;entryX=0;entryY=0.75;')
    d.edge('p1', 'p2', 'thông tin đăng nhập ⇄\nuserId, vai trò', 'startArrow=open;exitX=0.85;exitY=0.15;entryX=0;entryY=0.6;')
    d.edge('d1', 'p2', 'userId', 'exitX=0;exitY=0.5;entryX=1;entryY=0.5;')
    d.edge('p1', 'p3', 'userId + yêu cầu', 'exitX=0.9;exitY=0.8;entryX=0;entryY=0.5;')
    d.edge('p3', 'd2', 'đọc / ghi lịch hẹn, ca', 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')
    d.edge('p3', 'p1', 'AppointmentDto', 'dashed=1;exitX=0.3;exitY=0.95;entryX=0.5;entryY=1;', [(926, 700), (520, 700)])
    d.text('note', 'Yourdon/DeMarco. Mọi luồng từ tác nhân ngoài đi vào đúng một tiến trình 1.0; tác nhân không chạm '
           'trực tiếp 2.0, 3.0 hay kho dữ liệu.', 40, 790, 2000, 50, 14)
    d.save('facade-dfd.drawio')


if __name__ == '__main__':
    class_diagram()
    sequence()
    erd()
    dfd()
    print('Generated Facade class, sequence, ERD and DFD in', OUT.relative_to(REPO))
