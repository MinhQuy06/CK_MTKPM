#!/usr/bin/env python3
"""Observer pattern — class, sequence, ERD and DFD (editable, black-and-white draw.io)."""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'common'))
from drawio_kit import (Diagram, Sequence, REPO, DEPENDS, EXTERNAL, PROCESS, STORE)  # noqa: E402

OUT = REPO / 'docs/observer/diagrams'


def class_diagram():
    d = Diagram('Observer — Class', 2700, 1100, OUT)
    d.title('MẪU OBSERVER — PHẢN ỨNG KHI ĐẶT / HỦY LỊCH — SƠ ĐỒ LỚP', 2600)
    d.uml('subject', '«ConcreteSubject»\nBookingServiceImpl', [
        '- events: ApplicationEventPublisher', '- pricing: PricingContext', '- …repositories', '',
        '+ createBooking(patientId, scheduleId, symptoms, expectation)', '  … lưu lịch hẹn, khóa ca …',
        '  events.publishEvent(new AppointmentBookedEvent(saved))',
        '+ cancelBooking(appointmentId, patientId)', '  … đổi trạng thái, mở ca thay thế …',
        '  events.publishEvent(new AppointmentCancelledEvent(a))'], 40, 110, 640)
    d.uml('bus', '«Subject registry · Spring»\nApplicationEventPublisher', [
        '+ publishEvent(event: Object)', '', 'Gọi đồng bộ mọi @EventListener', 'nhận kiểu sự kiện đó'],
          820, 110, 480)
    d.uml('booked', '«record · Event»\nAppointmentBookedEvent', ['appointment: Appointment'], 820, 460, 480)
    d.uml('cancelled', '«record · Event»\nAppointmentCancelledEvent', ['appointment: Appointment'], 820, 650, 480)
    d.uml('email', '«ConcreteObserver»\nAppointmentEmailObserver', [
        '- notifications: AsyncEmailNotificationService', '',
        '+ onBooked(e: AppointmentBookedEvent)  @EventListener',
        '+ onCancelled(e: AppointmentCancelledEvent)  @EventListener', '',
        'Lỗi chỉ ghi log, không hủy lịch đã đặt'], 1440, 110, 620)
    d.uml('cache', '«ConcreteObserver»\nSlotCacheObserver', [
        '- cache: RedisCacheService', '',
        '+ onBooked(e: AppointmentBookedEvent)  @EventListener',
        '+ onCancelled(e: AppointmentCancelledEvent)  @EventListener',
        '- evict(slot)  // bác sĩ + ngày làm việc'], 1440, 560, 620)
    d.uml('notify', '«interface»\nAsyncEmailNotificationService', [
        '+ sendBookingConfirmation(a)', '+ sendCancellationNotice(a)', '(bản đã bọc bởi Decorator)'], 2200, 130, 460)
    d.uml('redis', '«interface»\nRedisCacheService', ['+ evictSlotCache(doctorId, date)'], 2200, 600, 460)
    d.edge('subject', 'bus', 'publishEvent', 'exitX=1;exitY=0.2;entryX=0;entryY=0.3;')
    d.edge('subject', 'booked', '«create»', DEPENDS + 'exitX=1;exitY=0.65;entryX=0;entryY=0.5;')
    d.edge('subject', 'cancelled', '«create»', DEPENDS + 'exitX=1;exitY=0.85;entryX=0;entryY=0.5;')
    d.edge('bus', 'email', 'thông báo', 'exitX=1;exitY=0.3;entryX=0;entryY=0.3;')
    d.edge('bus', 'cache', 'thông báo', 'exitX=0.9;exitY=1;entryX=0;entryY=0.2;', [(1357, 595)])
    d.edge('email', 'notify', 'dùng', 'exitX=1;exitY=0.4;entryX=0;entryY=0.4;')
    d.edge('cache', 'redis', 'dùng', 'exitX=1;exitY=0.4;entryX=0;entryY=0.5;')
    d.text('note', 'Trước: BookingServiceImpl gọi thẳng dịch vụ email và cache. Sau: nó chỉ phát sự kiện; thêm phản ứng mới = '
           'thêm một observer, không sửa dịch vụ đặt lịch.\nObserver chạy trong giao dịch đặt lịch; email vẫn chỉ đi sau '
           'commit (AFTER_COMMIT). Đặt lịch thất bại ném lỗi trước khi phát sự kiện nên không observer nào chạy.',
           40, 960, 2620, 70, 15)
    d.save('observer-class.drawio')


def sequence():
    s = Sequence('Observer — Sequence: đặt lịch phát sự kiện cho các observer', [
        'Bệnh nhân (UI)', 'BookingFacade', 'BookingServiceImpl\n«Subject»', 'ApplicationEvent\nPublisher',
        'AppointmentEmail\nObserver', 'SlotCache\nObserver', 'AsyncEmailNotification\nService «decorated»',
        'RedisCacheService'], OUT, col=260)
    s.call(0, 1, 'POST /appointments/confirm')
    s.call(1, 2, 'createBooking(patientId, scheduleId, symptoms, expectation)')
    s.step(2, 'BookingOffer khóa + tính giá (Strategy),\nlưu Appointment, đánh dấu ca đã kín', 380, 56)
    s.begin('alt [ca đã kín / giá đổi / mất quyền hưởng]')
    s.reply(2, 1, 'BookingRejectionException → rollback, KHÔNG phát sự kiện')
    s.end()
    s.call(2, 3, 'publishEvent(AppointmentBookedEvent(saved))')
    s.call(3, 4, 'onBooked(event)')
    s.call(4, 6, 'sendBookingConfirmation(appointment)')
    s.reply(6, 4, 'đăng ký email gửi sau commit')
    s.call(3, 5, 'onBooked(event)')
    s.call(5, 7, 'evictSlotCache(doctorId, workDate)')
    s.reply(3, 2, 'mọi observer đã chạy (đồng bộ)')
    s.reply(2, 1, 'AppointmentDto')
    s.reply(1, 0, '201 Đặt lịch khám thành công')
    s.remark('Hủy lịch đi cùng đường: cancelBooking → publishEvent(AppointmentCancelledEvent) → onCancelled ở cả hai '
             'observer (email hủy + xóa cache ca).')
    s.footer('Observer email bắt mọi lỗi và chỉ ghi log, nên lỗi gửi thông báo không làm hỏng việc đặt lịch. '
             'Thứ tự giữa các observer không được dựa vào.')
    s.save('observer-sequence.drawio')


def erd():
    d = Diagram('Observer — ERD', 2000, 900, OUT)
    d.title('MẪU OBSERVER — ERD: DỮ LIỆU CỦA SỰ KIỆN ĐẶT / HỦY LỊCH', 1900)
    d.table('patients', 'patients', [('PK,FK', 'user_id', 'BIGINT'), ('', 'full_name', 'VARCHAR(255)'), ('', '…', '')],
            60, 140, 460)
    d.table('appointments', 'appointments  (mang trong sự kiện)', [
        ('PK', 'id', 'BIGINT'), ('', 'booking_code', 'VARCHAR(100) UK'), ('FK', 'patient_id', 'BIGINT'),
        ('FK', 'doctor_id', 'BIGINT'), ('FK', 'schedule_id', 'BIGINT UK'), ('', 'status', 'CONFIRMED | CANCELLED'),
        ('', 'price', 'DECIMAL(12,2)'), ('', '…', '')], 680, 140, 560)
    d.table('schedules', 'schedules', [('PK', 'id', 'BIGINT'), ('FK', 'doctor_id', 'BIGINT → khóa cache'),
                                       ('', 'work_date', 'DATE → khóa cache'), ('', 'start_time', 'TIME'),
                                       ('', 'end_time', 'TIME'), ('', 'is_available', 'BOOLEAN')], 1400, 140, 520)
    d.table('doctors', 'doctors', [('PK,FK', 'user_id', 'BIGINT'), ('', 'full_name', 'VARCHAR(255)'), ('', '…', '')],
            1400, 520, 520)
    d.box('cachekey', 'Redis (không phải bảng)\nslots:doctor:{doctorId}:date:{workDate}\n= danh sách ca trống đã cache',
          680, 560, 560, 110, 'whiteSpace=wrap;fontSize=15;dashed=1;')
    d.rel('appointments', 'patients', 'patient_id', 'ERzeroToMany', 'ERmandOne', 'exitX=0;exitY=0.4;entryX=1;entryY=0.4;')
    d.rel('appointments', 'schedules', 'schedule_id', 'ERzeroToOne', 'ERmandOne', 'exitX=1;exitY=0.4;entryX=0;entryY=0.4;')
    d.rel('schedules', 'doctors', 'doctor_id', 'ERzeroToMany', 'ERmandOne', 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
    d.edge('schedules', 'cachekey', 'SlotCacheObserver xóa', 'dashed=1;exitX=0;exitY=0.9;entryX=1;entryY=0.5;')
    d.text('legend', 'Observer không thêm bảng. Sự kiện mang Appointment vừa lưu; observer email đọc bệnh nhân/bác sĩ/ca để '
           'dựng bản tin, observer cache dùng doctor_id + work_date của ca làm khóa Redis.\n'
           'Ký hiệu crow\'s foot: ┤├ đúng một · o┤ không hoặc một · o< không hoặc nhiều.', 60, 760, 1880, 70, 15)
    d.save('observer-erd.drawio')


def dfd():
    d = Diagram('Observer — DFD', 2100, 900, OUT)
    d.title('MẪU OBSERVER — DFD MỨC 1: TỪ ĐẶT / HỦY LỊCH ĐẾN CÁC PHẢN ỨNG', 2000)
    d.box('patient', 'Bệnh nhân', 40, 330, 220, 130, EXTERNAL)
    d.box('p1', '1.0\nĐặt / hủy lịch\n(Subject)', 400, 300, 230, 190, PROCESS)
    d.box('p2', '2.0\nPhát sự kiện\nlịch hẹn', 800, 300, 220, 190, PROCESS)
    d.box('p3', '3.0\nGửi thông báo\n(observer email)', 1200, 150, 230, 190, PROCESS)
    d.box('p4', '4.0\nLàm mới cache\nca trống', 1200, 460, 230, 190, PROCESS)
    d.box('mail', 'Chuỗi thông báo\n(Decorator → email,\nSMS)', 1650, 180, 300, 130, EXTERNAL)
    d.box('d1', 'D1   appointments · schedules', 330, 650, 380, 56, STORE)
    d.box('d2', 'D2   Redis: ca trống đã cache', 1600, 650, 380, 56, STORE)
    d.edge('patient', 'p1', 'yêu cầu đặt / hủy', 'exitX=1;exitY=0.35;entryX=0;entryY=0.4;')
    d.edge('p1', 'patient', 'kết quả', 'exitX=0;exitY=0.65;entryX=1;entryY=0.65;')
    d.edge('p1', 'd1', 'lịch hẹn, trạng thái ca', 'exitX=0.4;exitY=1;entryX=0.4;entryY=0;')
    d.edge('p1', 'p2', 'lịch hẹn đã lưu', 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')
    d.edge('p2', 'p3', 'AppointmentBooked /\nCancelled', 'exitX=0.85;exitY=0.15;entryX=0;entryY=0.5;')
    d.edge('p2', 'p4', 'AppointmentBooked /\nCancelled', 'exitX=0.85;exitY=0.85;entryX=0;entryY=0.5;')
    d.edge('p3', 'mail', 'lịch hẹn + loại\nthông báo', 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')
    d.edge('p4', 'd2', 'xóa khóa\nbác sĩ + ngày', 'exitX=1;exitY=0.7;entryX=0.3;entryY=0;')
    d.text('note', 'Yourdon/DeMarco. Nhánh từ chối (ca kín, đổi giá, mất quyền hưởng) dừng ở 1.0: không có luồng tới 2.0, '
           'nên không có thông báo hay thao tác cache.', 40, 790, 2000, 50, 14)
    d.save('observer-dfd.drawio')


if __name__ == '__main__':
    class_diagram()
    sequence()
    erd()
    dfd()
    print('Generated Observer class, sequence, ERD and DFD in', OUT.relative_to(REPO))
