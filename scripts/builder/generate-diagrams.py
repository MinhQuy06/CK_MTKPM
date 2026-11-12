#!/usr/bin/env python3
"""Builder pattern — class, sequence, ERD and DFD (editable, black-and-white draw.io)."""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'common'))
from drawio_kit import (Diagram, Sequence, REPO, DEPENDS, EXTERNAL, PROCESS, STORE)  # noqa: E402

OUT = REPO / 'docs/builder/diagrams'


def class_diagram():
    d = Diagram('Builder — Class', 2000, 1180, OUT)
    d.title('MẪU BUILDER — DỰNG BẢN TIN THÔNG BÁO LỊCH HẸN — SƠ ĐỒ LỚP', 1900)
    d.uml('client', '«Client»\nAsyncEmailNotificationServiceImpl', [
        '- eventPublisher: ApplicationEventPublisher', '- director: NotificationMessageDirector', '',
        '+ sendBookingConfirmation(a: Appointment)', '+ sendAppointmentReminder(a: Appointment)',
        '+ sendCancellationNotice(a: Appointment)', '- publish(m: EmailNotificationMessage)'], 40, 110, 560)
    d.uml('director', '«Director»\nNotificationMessageDirector', [
        '+ BOOKING_CONFIRMED, APPOINTMENT_REMINDER,', '  BOOKING_CANCELLED: String', '',
        '+ bookingConfirmed(a: Appointment): EmailNotificationMessage',
        '+ reminder(a: Appointment): EmailNotificationMessage',
        '+ cancelled(a: Appointment): EmailNotificationMessage',
        '- forAppointment(type, a): EmailNotificationMessage'], 720, 110, 620)
    d.uml('builder', '«Builder»\nNotificationMessageBuilder', [
        '- type, recipientEmail, patientName, bookingCode,', '  doctorName, workDate, startTime, endTime,',
        '  price: String', '', '+ aMessage(): NotificationMessageBuilder  {static}',
        '+ type(t) / recipientEmail(e) / patientName(n) /', '  bookingCode(c) / doctorName(n) / workDate(d) /',
        '  startTime(t) / endTime(t) / price(p): NotificationMessageBuilder',
        '+ build(): EmailNotificationMessage', '- require(field, value)  // type, recipientEmail, bookingCode'],
          720, 520, 620)
    d.uml('product', '«Product · record»\nEmailNotificationMessage', [
        'type: String', 'recipientEmail: String', 'patientName: String', 'bookingCode: String',
        'doctorName: String', 'workDate: String', 'startTime: String', 'endTime: String',
        'price: String  (có thể null)'], 1460, 520, 480)
    d.uml('source', '«Entity»\nAppointment', [
        'bookingCode', 'patient → user.email, fullName', 'doctor → fullName',
        'schedule → workDate, startTime, endTime', 'price'], 1460, 110, 480)
    d.uml('event', '«record»\nEmailNotificationRequestedEvent', ['message: EmailNotificationMessage'], 40, 520, 560)
    d.edge('client', 'director', 'dùng', 'exitX=1;exitY=0.3;entryX=0;entryY=0.3;')
    d.edge('director', 'builder', 'điều khiển các bước dựng', 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
    d.edge('builder', 'product', '«create»', DEPENDS + 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')
    d.edge('director', 'source', 'đọc dữ liệu', DEPENDS + 'exitX=1;exitY=0.3;entryX=0;entryY=0.3;')
    d.edge('client', 'event', 'publishEvent', DEPENDS + 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
    d.edge('event', 'product', 'mang theo', 'exitX=0.5;exitY=1;entryX=0.5;entryY=1;', [(320, 1000), (1700, 1000)])
    d.text('note', 'Builder thay constructor 9 tham số String dễ truyền nhầm thứ tự. Director giữ "công thức" cho từng loại '
           'thông báo; nơi gọi chỉ nói "bản tin hủy cho lịch hẹn này".\nbuild() từ chối khi thiếu type, recipientEmail '
           'hoặc bookingCode (IllegalStateException); price được phép null cho lịch hẹn cũ.', 40, 1040, 1900, 70, 15)
    d.save('builder-class.drawio')


def sequence():
    s = Sequence('Builder — Sequence: dựng bản tin xác nhận đặt lịch', [
        'AppointmentEmailObserver', 'AsyncEmailNotification\nServiceImpl', 'NotificationMessage\nDirector',
        'NotificationMessage\nBuilder', 'Spring event bus', 'EmailQueuePublisher\n«AFTER_COMMIT»'], OUT)
    s.call(0, 1, 'sendBookingConfirmation(appointment)')
    s.call(1, 2, 'bookingConfirmed(appointment)')
    s.call(2, 3, 'aMessage()')
    s.reply(3, 2, 'builder')
    s.call(2, 3, 'type("BOOKING_CONFIRMED").recipientEmail(…).patientName(…)')
    s.call(2, 3, '.bookingCode(…).doctorName(…).workDate(…).startTime(…).endTime(…).price(…)')
    s.call(2, 3, 'build()')
    s.begin('alt [thiếu type / recipientEmail / bookingCode]')
    s.reply(3, 2, 'IllegalStateException("…missing required field: <tên trường>")')
    s.end()
    s.reply(3, 2, 'EmailNotificationMessage (9 trường)')
    s.reply(2, 1, 'EmailNotificationMessage')
    s.call(1, 4, 'publishEvent(EmailNotificationRequestedEvent(message))')
    s.remark('Giao dịch đặt lịch commit → Spring gọi listener AFTER_COMMIT. Rollback thì không có bản tin nào được gửi.')
    s.call(4, 5, 'publishAfterCommit(event)  «async»')
    s.step(5, 'rabbitTemplate.convertAndSend(exchange,\nroutingKey, message)', 300, 56)
    s.footer('Reminder và cancelled đi cùng đường, chỉ khác type. Builder không ghi DB; bản tin đi ra hàng đợi email '
             '(RabbitMQ).')
    s.save('builder-sequence.drawio')


def erd():
    d = Diagram('Builder — ERD', 2000, 980, OUT)
    d.title('MẪU BUILDER — ERD: CÁC BẢNG MÀ DIRECTOR ĐỌC ĐỂ DỰNG BẢN TIN', 1900)
    d.table('users', 'users', [('PK', 'id', 'BIGINT'), ('', 'email', 'VARCHAR(255) UK  → recipientEmail'),
                               ('', 'role', 'VARCHAR(20)'), ('', '…', '')], 60, 120, 520)
    d.table('patients', 'patients', [('PK,FK', 'user_id', 'BIGINT'), ('', 'full_name', 'VARCHAR(255) → patientName'),
                                     ('', 'phone', 'VARCHAR(20)'), ('', '…', '')], 60, 470, 520)
    d.table('appointments', 'appointments', [
        ('PK', 'id', 'BIGINT'), ('', 'booking_code', 'VARCHAR(100) UK → bookingCode'), ('FK', 'patient_id', 'BIGINT'),
        ('FK', 'doctor_id', 'BIGINT'), ('FK', 'schedule_id', 'BIGINT UK'), ('', 'status', 'VARCHAR(20)'),
        ('', 'price', 'DECIMAL(12,2) → price'), ('', '…', '')], 720, 300, 560)
    d.table('doctors', 'doctors', [('PK,FK', 'user_id', 'BIGINT'), ('', 'full_name', 'VARCHAR(255) → doctorName'),
                                   ('', '…', '')], 1420, 120, 520)
    d.table('schedules', 'schedules', [('PK', 'id', 'BIGINT'), ('FK', 'doctor_id', 'BIGINT'),
                                       ('', 'work_date', 'DATE → workDate'), ('', 'start_time', 'TIME → startTime'),
                                       ('', 'end_time', 'TIME → endTime')], 1420, 470, 520)
    d.rel('patients', 'users', 'user_id (1–1)', 'ERzeroToOne', 'ERmandOne', 'exitX=0.5;exitY=0;entryX=0.5;entryY=1;')
    d.rel('appointments', 'patients', 'patient_id', 'ERzeroToMany', 'ERmandOne', 'exitX=0;exitY=0.6;entryX=1;entryY=0.4;')
    d.rel('appointments', 'doctors', 'doctor_id', 'ERzeroToMany', 'ERmandOne', 'exitX=1;exitY=0.2;entryX=0;entryY=0.5;')
    d.rel('appointments', 'schedules', 'schedule_id', 'ERzeroToOne', 'ERmandOne', 'exitX=1;exitY=0.7;entryX=0;entryY=0.5;')
    d.rel('schedules', 'doctors', 'doctor_id', 'ERzeroToMany', 'ERmandOne', 'exitX=0.5;exitY=0;entryX=0.5;entryY=1;')
    d.text('legend', 'Builder không thêm bảng và không ghi DB: bản tin chỉ tồn tại trong bộ nhớ rồi đi ra hàng đợi email. '
           'Mũi tên "→ trường" cho biết cột nào thành trường nào của EmailNotificationMessage.\n'
           'Ký hiệu crow\'s foot: ┤├ đúng một · o┤ không hoặc một · o< không hoặc nhiều. Bảng chỉ vẽ cột liên quan (…).',
           60, 830, 1880, 70, 15)
    d.save('builder-erd.drawio')


def dfd():
    d = Diagram('Builder — DFD', 2000, 900, OUT)
    d.title('MẪU BUILDER — DFD MỨC 1: TỪ LỊCH HẸN ĐẾN BẢN TIN EMAIL', 1900)
    d.box('booking', 'Luồng đặt / hủy lịch\n(Observer email)', 40, 330, 260, 130, EXTERNAL)
    d.box('queue', 'Hàng đợi email\n(RabbitMQ → Bệnh nhân)', 1700, 330, 260, 130, EXTERNAL)
    d.box('p1', '1.0\nChọn công thức\n(Director)', 420, 300, 220, 190, PROCESS)
    d.box('p2', '2.0\nDựng & kiểm tra\n(Builder)', 830, 300, 220, 190, PROCESS)
    d.box('p3', '3.0\nXếp hàng sau\ncommit', 1260, 300, 220, 190, PROCESS)
    d.box('d1', 'D1   appointments · schedules', 360, 650, 380, 56, STORE)
    d.box('d2', 'D2   patients · users · doctors', 820, 650, 400, 56, STORE)
    d.edge('booking', 'p1', 'lịch hẹn +\nloại thông báo', 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')
    d.edge('d1', 'p1', 'mã lịch, ngày,\ngiờ, giá', 'exitX=0.4;exitY=0;entryX=0.4;entryY=1;')
    d.edge('d2', 'p1', 'email, tên bệnh nhân,\ntên bác sĩ', 'exitX=0.2;exitY=0;entryX=0.9;entryY=0.85;')
    d.edge('p1', 'p2', 'các bước dựng\n(type, email, …)', 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')
    d.edge('p2', 'p3', 'EmailNotificationMessage\nhợp lệ', 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')
    d.edge('p3', 'queue', 'bản tin', 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')
    d.edge('p2', 'booking', 'thiếu trường bắt buộc → lỗi', 'dashed=1;exitX=0.5;exitY=0;entryX=0.5;entryY=0;',
           [(940, 220), (170, 220)])
    d.text('note', 'Yourdon/DeMarco: hình chữ nhật = tác nhân ngoài, hình tròn = tiến trình, kho mở một bên = kho dữ liệu. '
           'Builder chỉ đọc D1, D2; không ghi kho nào.', 40, 790, 1900, 50, 14)
    d.save('builder-dfd.drawio')


def use_case():
    d = Diagram('Builder — Use Case', 1800, 900, OUT)
    d.title('MẪU BUILDER — DỰNG BẢN TIN THÔNG BÁO — SƠ ĐỒ USE CASE', 1700)
    d.system('Hệ thống đặt lịch khám — Thông báo lịch hẹn (Builder)', 280, 80, 1260, 720)
    d.actor('patient', 'Bệnh nhân', 120, 330)
    d.actor('job', 'Job nhắc lịch\n(chưa có)', 120, 600)
    d.actor('queue', 'Hàng đợi email\n(RabbitMQ)', 1640, 90)
    d.case('uc-book', 'Đặt lịch khám', 330, 150)
    d.case('uc-cancel', 'Hủy lịch khám', 330, 330)
    d.case('uc-remind', 'Nhắc lịch khám', 330, 600)
    d.case('uc-notify', 'Gửi thông báo lịch hẹn', 780, 330)
    d.case('uc-recipe', 'Chọn công thức bản tin\n(Director)', 1170, 170)
    d.case('uc-build', 'Dựng bản tin từng trường\n(Builder)', 1170, 330)
    d.case('uc-check', 'Kiểm tra trường bắt buộc\n(type, email, mã lịch)', 1170, 500)
    d.case('uc-missing', 'Báo lỗi thiếu trường', 780, 620)
    d.assoc('patient', 'uc-book')
    d.assoc('patient', 'uc-cancel')
    d.assoc('job', 'uc-remind')
    d.edge('uc-notify', 'queue', '', 'edgeStyle=none;endArrow=none;exitX=0.5;exitY=0;', points=[(940, 130)])
    for base in ('uc-book', 'uc-cancel', 'uc-remind'):
        d.include(base, 'uc-notify')
    d.include('uc-notify', 'uc-recipe')
    d.include('uc-recipe', 'uc-build')
    d.include('uc-build', 'uc-check')
    d.extend('uc-missing', 'uc-check', 'thiếu type / email / mã lịch')
    d.text('scope', 'Phạm vi: bản tin email cho đặt / hủy / nhắc lịch. Dịch vụ đã hỗ trợ nhắc lịch nhưng chưa có job gọi '
           '(actor đánh dấu "chưa có"). Builder không ghi DB.', 280, 815, 1260, 50, 14)
    d.save('builder-use-case.drawio')


if __name__ == '__main__':
    class_diagram()
    sequence()
    erd()
    dfd()
    use_case()
    print('Generated Builder class, sequence, ERD, DFD and use case in', OUT.relative_to(REPO))
