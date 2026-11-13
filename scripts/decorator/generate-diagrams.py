#!/usr/bin/env python3
"""Decorator pattern — class, sequence, ERD and DFD (editable, black-and-white draw.io)."""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'common'))
from drawio_kit import (Diagram, Sequence, REPO, DEPENDS, IMPLEMENTS, EXTENDS, AGGREGATES,  # noqa: E402
                        EXTERNAL, PROCESS, STORE)

OUT = REPO / 'docs/decorator/diagrams'


def class_diagram():
    d = Diagram('Decorator — Class', 2200, 1320, OUT)
    d.title('MẪU DECORATOR — BỌC DỊCH VỤ THÔNG BÁO — SƠ ĐỒ LỚP', 2100)
    d.uml('client', '«Client»\nAppointmentEmailObserver', ['- notifications: AsyncEmailNotificationService',
                                                           '(nhận bean @Primary đã bọc)'], 40, 110, 520)
    d.uml('component', '«interface · Component»\nAsyncEmailNotificationService', [
        '+ sendBookingConfirmation(a: Appointment)', '+ sendAppointmentReminder(a: Appointment)',
        '+ sendCancellationNotice(a: Appointment)'], 760, 110, 560)
    d.uml('concrete', '«ConcreteComponent»\nAsyncEmailNotificationServiceImpl', [
        'Dựng bản tin (Builder) và phát', 'EmailNotificationRequestedEvent → RabbitMQ', 'sau commit'], 40, 450, 520)
    d.uml('base', '«abstract · Decorator»\nNotificationServiceDecorator', [
        '# inner: AsyncEmailNotificationService', '', '+ send…(a) { inner.send…(a) }  // mặc định chuyển tiếp'],
          1500, 110, 640)
    d.uml('dedup', '«ConcreteDecorator» 1 — ngoài cùng\nDeduplicationDecorator', [
        '- history: NotificationLogRepository', '+ send…(a): đã có EMAIL SENT cùng type + mã', '  → ghi SKIPPED_DUPLICATE, dừng',
        '  ngược lại → inner.send…(a)'], 760, 520, 600)
    d.uml('sms', '«ConcreteDecorator» 2\nSmsNotificationDecorator', [
        '- channel: NotificationChannel  (Adapter)', '- director: NotificationMessageDirector',
        '- history: NotificationLogRepository', '+ send…(a): inner.send…(a), rồi SMS', '  ghi SMS SENT / SKIPPED_NO_PHONE / SKIPPED_DISABLED / FAILED',
        '  lỗi SMS không ném ra ngoài'], 1440, 520, 680)
    d.uml('audit', '«ConcreteDecorator» 3 — sát email\nAuditLogDecorator', [
        '- history: NotificationLogRepository', '+ send…(a): inner.send…(a)', '  → ghi EMAIL SENT (lỗi → FAILED, ném lại)'],
          760, 880, 600)
    d.uml('config', '«Configuration»\nNotificationDecoratorConfig', [
        '+ decoratedNotificationService(…)  @Bean @Primary', '  new Dedup(', '    new Sms(', '      new Audit(email)))'],
          40, 830, 520)
    d.uml('repo', '«Repository»\nNotificationLogRepository', [
        '+ existsByTypeAndBookingCodeAndChannelAndStatus(…)', '+ findByBookingCodeOrderByIdAsc(code)'], 1440, 960, 680)
    d.edge('client', 'component', 'dùng', 'exitX=1;exitY=0.5;entryX=0;entryY=0.4;')
    d.edge('concrete', 'component', '', IMPLEMENTS + 'exitX=1;exitY=0.3;entryX=0.2;entryY=1;')
    d.edge('base', 'component', '', IMPLEMENTS + 'exitX=0;exitY=0.3;entryX=1;entryY=0.3;')
    d.edge('base', 'component', 'inner 1', AGGREGATES + 'exitX=0;exitY=0.8;entryX=1;entryY=0.8;')
    for key in ('dedup', 'sms'):
        d.edge(key, 'base', '', EXTENDS + 'exitX=0.5;exitY=0;entryX=0.5;entryY=1;')
    d.edge('audit', 'base', '', EXTENDS + 'exitX=1;exitY=0.3;entryX=0.95;entryY=1;', [(2160, 920), (2160, 480), (2108, 480)])
    d.edge('config', 'dedup', '«create» chuỗi', DEPENDS + 'exitX=1;exitY=0.2;entryX=0;entryY=0.6;')
    d.edge('audit', 'repo', 'ghi', DEPENDS + 'exitX=1;exitY=0.7;entryX=0;entryY=0.5;')
    d.text('note', 'Thứ tự từ ngoài vào trong: chống trùng → SMS → ghi lịch sử → email gốc. Chống trùng đứng trước SMS nên '
           'lần gửi trùng không sinh SMS lần hai; ghi lịch sử nằm sát email nên chỉ ghi lần gửi thật.\n'
           'Thêm/bớt/đổi thứ tự hành vi = sửa một chỗ NotificationDecoratorConfig, không sửa dịch vụ email gốc.',
           40, 1180, 2100, 70, 15)
    d.save('decorator-class.drawio')


def sequence():
    s = Sequence('Decorator — Sequence: xác nhận đặt lịch đi qua chuỗi bọc', [
        'AppointmentEmail\nObserver', 'Deduplication\nDecorator', 'SmsNotification\nDecorator', 'AuditLog\nDecorator',
        'AsyncEmailNotification\nServiceImpl', 'NotificationLog\nRepository', 'NotificationChannel\n(SMS Adapter)'],
        OUT, col=280)
    s.call(0, 1, 'sendBookingConfirmation(a)')
    s.call(1, 5, 'existsBy…(BOOKING_CONFIRMED, code, EMAIL, SENT)')
    s.reply(5, 1, 'true / false')
    s.begin('alt [đã gửi trước đó]')
    s.call(1, 5, 'save(… EMAIL SKIPPED_DUPLICATE)')
    s.reply(1, 0, 'dừng: không email, không SMS')
    s.end()
    s.call(1, 2, 'sendBookingConfirmation(a)')
    s.call(2, 3, 'sendBookingConfirmation(a)')
    s.call(3, 4, 'sendBookingConfirmation(a)')
    s.reply(4, 3, 'đã xếp hàng email (sau commit)')
    s.call(3, 5, 'save(… EMAIL SENT)')
    s.reply(3, 2, '')
    s.call(2, 6, 'send(patient.phone, director.bookingConfirmed(a))')
    s.reply(6, 2, 'DeliveryResult (SENT / SKIPPED_NO_PHONE / SKIPPED_DISABLED / FAILED)')
    s.begin('alt [kênh SMS ném ngoại lệ]')
    s.step(2, 'bắt lỗi → DeliveryResult(FAILED, lý do)', 360, 44)
    s.end()
    s.call(2, 5, 'save(… SMS <status>)')
    s.reply(2, 1, '')
    s.reply(1, 0, '')
    s.footer('Nhắc lịch và hủy lịch đi cùng chuỗi. Mọi dòng lịch sử nằm trong giao dịch đặt lịch nên rollback thì không '
             'còn dòng nào. Giới hạn: SMS gửi ngay trong giao dịch, email đi sau commit.')
    s.save('decorator-sequence.drawio')


def erd():
    d = Diagram('Decorator — ERD', 2000, 860, OUT)
    d.title('MẪU DECORATOR — ERD: BẢNG LỊCH SỬ THÔNG BÁO notification_log', 1900)
    d.table('appointments', 'appointments', [('PK', 'id', 'BIGINT'), ('', 'booking_code', 'VARCHAR(100) UK'),
                                             ('FK', 'patient_id', 'BIGINT'), ('', 'status', 'VARCHAR(20)'), ('', '…', '')],
            60, 160, 520)
    d.table('patients', 'patients', [('PK,FK', 'user_id', 'BIGINT'), ('', 'phone', 'VARCHAR(20) NULL'), ('', '…', '')],
            60, 520, 520)
    d.table('log', 'notification_log  «new»', [
        ('PK', 'id', 'BIGINT AUTO_INCREMENT'), ('', 'type', 'VARCHAR(40) NOT NULL'),
        ('', '', '(BOOKING_CONFIRMED | APPOINTMENT_REMINDER | BOOKING_CANCELLED)'),
        ('', 'booking_code', 'VARCHAR(50) NOT NULL'), ('', 'channel', 'VARCHAR(10) NOT NULL  (EMAIL | SMS)'),
        ('', 'status', 'VARCHAR(30) NOT NULL'),
        ('', '', '(SENT | FAILED | SKIPPED_DUPLICATE | SKIPPED_NO_PHONE | SKIPPED_DISABLED)'), ('', 'note', 'VARCHAR(255) NULL'),
        ('', 'created_at', 'DATETIME(6) NOT NULL'), ('', '', ''), ('IDX', 'idx_notification_log_booking', ''),
        ('', '  (booking_code, type)', '')], 800, 160, 900, new=True)
    d.rel('appointments', 'patients', 'patient_id', 'ERzeroToMany', 'ERmandOne', 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
    d.edge('log', 'appointments', '',
           'dashed=1;startArrow=ERzeroToMany;endArrow=ERmandOne;startFill=0;endFill=0;startSize=14;endSize=14;'
           'exitX=0;exitY=0.3;entryX=1;entryY=0.4;')
    d.text('link', 'booking_code\n(liên kết logic, không FK):\n1 lịch hẹn — 0..* dòng lịch sử', 600, 330, 200, 80, 13)
    d.text('legend', 'Không đặt FK để lịch sử vẫn ghi được khi lịch hẹn bị xóa và để bảng độc lập với luồng đặt lịch. '
           'Chống trùng tra (type, booking_code, channel=EMAIL, status=SENT) qua chỉ mục.\nHibernate tạo bảng '
           '(ddl-auto); MySQL nâng cấp tay bằng db/upgrade/decorator/001-notification-log.sql.',
           60, 740, 1880, 70, 15)
    d.save('decorator-erd.drawio')


def dfd():
    d = Diagram('Decorator — DFD', 2200, 920, OUT)
    d.title('MẪU DECORATOR — DFD MỨC 1: MỘT YÊU CẦU THÔNG BÁO ĐI QUA CHUỖI BỌC', 2100)
    d.box('obs', 'Observer email\n(đặt / hủy / nhắc)', 40, 300, 240, 130, EXTERNAL)
    d.box('p1', '1.0\nChống gửi\ntrùng', 380, 270, 210, 190, PROCESS)
    d.box('p2', '2.0\nGửi email\n+ ghi lịch sử', 760, 270, 210, 190, PROCESS)
    d.box('p3', '3.0\nGửi SMS\n+ ghi kết quả', 1180, 270, 210, 190, PROCESS)
    d.box('queue', 'Hàng đợi email\n(RabbitMQ)', 760, 90, 210, 100, EXTERNAL)
    d.box('vendor', 'Kênh SMS\n(Adapter → nhà cung cấp)', 1600, 300, 300, 130, EXTERNAL)
    d.box('d1', 'D1   notification_log', 700, 680, 360, 56, STORE)
    d.box('d2', 'D2   patients (phone)', 1180, 680, 320, 56, STORE)
    d.edge('obs', 'p1', 'loại + lịch hẹn', 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')
    d.edge('d1', 'p1', 'đã EMAIL SENT?', 'exitX=0;exitY=0.3;entryX=0.4;entryY=1;', [(470, 697)])
    d.edge('p1', 'd1', 'SKIPPED_DUPLICATE', 'dashed=1;exitX=0.75;exitY=0.95;entryX=0.1;entryY=0;')
    d.edge('p1', 'p2', 'lần đầu', 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')
    d.edge('p2', 'queue', 'bản tin email', 'exitX=0.5;exitY=0;entryX=0.5;entryY=1;')
    d.edge('p2', 'd1', 'EMAIL SENT / FAILED', 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
    d.edge('p2', 'p3', 'email xong', 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')
    d.edge('d2', 'p3', 'phone', 'exitX=0.5;exitY=0;entryX=0.5;entryY=1;')
    d.edge('p3', 'vendor', 'số + bản tin', 'exitX=1;exitY=0.4;entryX=0;entryY=0.4;')
    d.edge('vendor', 'p3', 'DeliveryResult', 'exitX=0;exitY=0.75;entryX=1;entryY=0.75;')
    d.edge('p3', 'd1', 'SMS SENT /\nSKIPPED_NO_PHONE /\nSKIPPED_DISABLED / FAILED', 'exitX=0.2;exitY=0.95;entryX=0.9;entryY=0;',
           [(1222, 600), (1024, 600)])
    d.text('note', 'Yourdon/DeMarco. Thứ tự tiến trình đúng thứ tự lớp bọc: 1.0 chống trùng (ngoài cùng) → 2.0 email '
           '(ghi lịch sử nằm sát email) → 3.0 SMS sau email.', 40, 810, 2100, 50, 14)
    d.save('decorator-dfd.drawio')


def use_case():
    d = Diagram('Decorator — Use Case', 1800, 1000, OUT)
    d.title('MẪU DECORATOR — BỌC DỊCH VỤ THÔNG BÁO — SƠ ĐỒ USE CASE', 1700)
    d.system('Hệ thống đặt lịch khám — Chuỗi thông báo (Decorator)', 280, 80, 1260, 820)
    d.actor('patient', 'Bệnh nhân', 120, 420)
    d.actor('queue', 'Hàng đợi email\n(RabbitMQ)', 1640, 390)
    d.actor('sms', 'Kênh SMS\n(Adapter)', 1640, 580)
    d.case('uc-notify', 'Nhận thông báo\nđặt / hủy / nhắc lịch', 330, 410)
    d.case('uc-dedup', 'Chống gửi trùng', 760, 260)
    d.case('uc-sms', 'Gửi thêm SMS', 760, 580)
    d.case('uc-mail', 'Gửi email', 760, 420)
    d.case('uc-log', 'Ghi lịch sử thông báo\n(notification_log)', 1170, 500)
    d.case('uc-skip', 'Bỏ qua lần gửi trùng', 760, 120)
    d.case('uc-smsfail', 'Ghi lỗi SMS, không chặn\nemail và đặt lịch', 760, 750)
    d.assoc('patient', 'uc-notify')
    d.assoc('uc-sms', 'sms')
    d.assoc('uc-mail', 'queue')
    for part in ('uc-dedup', 'uc-sms', 'uc-mail'):
        d.include('uc-notify', part)
    d.include('uc-mail', 'uc-log')
    d.include('uc-sms', 'uc-log')
    d.extend('uc-skip', 'uc-dedup', 'đã gửi email cùng loại')
    d.extend('uc-smsfail', 'uc-sms', 'kênh SMS lỗi')
    d.text('scope', 'Mỗi use case bên trong là một lớp bọc; thứ tự chạy: chống trùng → SMS → ghi lịch sử → email. '
           'Thêm hành vi (ví dụ thử lại) = thêm một use case / lớp bọc, không sửa "Gửi email".',
           280, 915, 1260, 50, 14)
    d.save('decorator-use-case.drawio')


if __name__ == '__main__':
    class_diagram()
    sequence()
    erd()
    dfd()
    use_case()
    print('Generated Decorator class, sequence, ERD, DFD and use case in', OUT.relative_to(REPO))
