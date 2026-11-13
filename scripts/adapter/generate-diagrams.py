#!/usr/bin/env python3
"""Adapter pattern — class, sequence, ERD and DFD (editable, black-and-white draw.io)."""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'common'))
from drawio_kit import (Diagram, Sequence, REPO, DEPENDS, IMPLEMENTS, AGGREGATES, EXTERNAL, PROCESS, STORE)  # noqa: E402

OUT = REPO / 'docs/adapter/diagrams'


def class_diagram():
    d = Diagram('Adapter — Class', 2100, 1320, OUT)
    d.title('MẪU ADAPTER — KÊNH SMS CỦA NHÀ CUNG CẤP — SƠ ĐỒ LỚP', 2000)
    d.uml('client', '«Client»\nSmsNotificationDecorator', [
        '- channel: NotificationChannel', '', '- alsoSend(appointment, message)',
        '  result = channel.send(patient.phone, message)'], 40, 110, 520)
    d.uml('target', '«interface · Target»\nNotificationChannel', [
        '+ name(): String', '+ send(patientPhone: String,', '       message: EmailNotificationMessage)',
        '       : DeliveryResult'], 700, 110, 520)
    d.uml('adapter', '«Adapter»\nSmsChannelAdapter', [
        '- gateway: SmsGatewayClient', '', '+ name(): "SMS"',
        '+ send(patientPhone, message): DeliveryResult', '  trống → SKIPPED_NO_PHONE',
        '  sai định dạng → FAILED', '  DISABLED → SKIPPED_DISABLED', '  mã nhà cung cấp ≠ 0 → FAILED', '~ text(m): String  // SMS ngắn, không dấu'],
          700, 470, 520)
    d.uml('adaptee', '«interface · Adaptee (giao diện nhà cung cấp)»\nSmsGatewayClient', [
        '+ OK = 0, REJECTED = 400, DISABLED = -1', '',
        '+ submitSms(msisdn: String, text: String): int'], 1400, 470, 640)
    d.uml('esms', '«Adaptee thật»\nEsmsGatewayClient', [
        'REST eSMS (esms.vn)', 'CodeResult 100 → OK, khác → REJECTED', 'lỗi mạng → ném ngoại lệ',
        'bật khi SMS_ENABLED=true'], 1400, 760, 310)
    d.uml('disabled', '«Adaptee khi tắt»\nDisabledSmsGatewayClient', [
        'không gửi, ghi log', 'luôn trả DISABLED', 'mặc định (SMS_ENABLED=false)'], 1730, 760, 310)
    d.uml('phone', '«utility»\nVietnamPhone', [
        '+ toInternational(stored: String): Optional<String>', '  "0912 345 678" → "+84912345678"'], 1400, 110, 640)
    d.uml('result', '«record»\nDeliveryResult', [
        'status: Status {SENT, SKIPPED_NO_PHONE,', '  SKIPPED_DISABLED, FAILED}', 'detail: String'], 40, 470, 520)
    d.edge('client', 'target', 'dùng', 'exitX=1;exitY=0.4;entryX=0;entryY=0.4;')
    d.edge('adapter', 'target', '', IMPLEMENTS + 'exitX=0.5;exitY=0;entryX=0.5;entryY=1;')
    d.edge('adapter', 'adaptee', 'gateway (composition)', AGGREGATES.replace('startFill=0', 'startFill=1')
           + 'exitX=1;exitY=0.4;entryX=0;entryY=0.4;')
    d.edge('adapter', 'phone', 'chuẩn hóa số', DEPENDS + 'exitX=0.85;exitY=0;entryX=0.47;entryY=1;',
           [(1142, 400), (1700, 400)])
    d.edge('adapter', 'result', '«create»', DEPENDS + 'exitX=0;exitY=0.4;entryX=1;entryY=0.4;')
    d.edge('esms', 'adaptee', '', IMPLEMENTS + 'exitX=0.5;exitY=0;entryX=0.25;entryY=1;')
    d.edge('disabled', 'adaptee', '', IMPLEMENTS + 'exitX=0.5;exitY=0;entryX=0.75;entryY=1;')
    d.text('note', 'Giao diện của nhà cung cấp khác giao diện phòng khám: số quốc tế (+84…), văn bản thô và mã trạng thái '
           'số thay vì ngoại lệ. SmsChannelAdapter dịch qua lại; phần còn lại của hệ thống chỉ biết NotificationChannel.\n'
           'SmsGatewayConfig chọn EsmsGatewayClient (thật) khi SMS_ENABLED=true, ngược lại DisabledSmsGatewayClient. '
           'Bản giả lập SimulatedSmsGatewayClient chỉ nằm trong src/test để kiểm chứng Adapter.',
           40, 1180, 2000, 80, 15)
    d.save('adapter-class.drawio')


def sequence():
    s = Sequence('Adapter — Sequence: gửi SMS qua adapter', [
        'SmsNotification\nDecorator «Client»', 'SmsChannelAdapter\n«Adapter»', 'VietnamPhone',
        'SmsGatewayClient\n«Adaptee»'], OUT, col=380)
    s.call(0, 1, 'send("0912 345 678", message)')
    s.begin('alt [số điện thoại null / rỗng]')
    s.reply(1, 0, 'DeliveryResult(SKIPPED_NO_PHONE) — không gọi nhà cung cấp')
    s.end()
    s.call(1, 2, 'toInternational("0912 345 678")')
    s.reply(2, 1, 'Optional("+84912345678")')
    s.begin('alt [số sai định dạng → Optional.empty()]')
    s.reply(1, 0, 'DeliveryResult(FAILED, "Số điện thoại không hợp lệ")')
    s.end()
    s.step(1, 'text(message) → "Da dat lich kham BK-…:\n2026-11-12 09:00-09:30, BS …"', 360, 56)
    s.call(1, 3, 'submitSms("+84912345678", text)')
    s.reply(3, 1, 'int code (0 = OK, 400 = REJECTED, -1 = DISABLED)')
    s.reply(1, 0, '0 → SENT · -1 → SKIPPED_DISABLED · khác → FAILED')
    s.footer('Nhà cung cấp ném ngoại lệ (timeout…) → Decorator bắt và ghi FAILED (xem Decorator). Adapter không biết gì về '
             'lịch sử thông báo.')
    s.save('adapter-sequence.drawio')


def erd():
    d = Diagram('Adapter — ERD', 2000, 860, OUT)
    d.title('MẪU ADAPTER — ERD: SỐ ĐIỆN THOẠI ĐẦU VÀO VÀ KẾT QUẢ ĐƯỢC GHI LẠI', 1900)
    d.table('users', 'users', [('PK', 'id', 'BIGINT'), ('', 'email', 'VARCHAR(255) UK'), ('', '…', '')], 60, 140, 440)
    d.table('patients', 'patients', [('PK,FK', 'user_id', 'BIGINT'),
                                     ('', 'phone', 'VARCHAR(20) UK NULL → patientPhone'), ('', 'full_name', 'VARCHAR(255)'),
                                     ('', '…', '')], 60, 450, 520)
    d.table('appointments', 'appointments', [('PK', 'id', 'BIGINT'), ('', 'booking_code', 'VARCHAR(100) UK'),
                                             ('FK', 'patient_id', 'BIGINT'), ('', '…', '')], 720, 450, 480)
    d.table('log', 'notification_log  «new» (Decorator ghi)', [
        ('PK', 'id', 'BIGINT'), ('', 'type', 'VARCHAR(40)'), ('', 'booking_code', 'VARCHAR(50)'),
        ('', 'channel', "VARCHAR(10)  = 'SMS'"), ('', 'status', 'SENT | SKIPPED_NO_PHONE | SKIPPED_DISABLED | FAILED'),
        ('', 'note', 'VARCHAR(255) ← DeliveryResult.detail'), ('', 'created_at', 'DATETIME(6)')], 1340, 140, 600, new=True)
    d.rel('patients', 'users', 'user_id (1–1)', 'ERzeroToOne', 'ERmandOne', 'exitX=0.5;exitY=0;entryX=0.5;entryY=1;')
    d.rel('appointments', 'patients', 'patient_id', 'ERzeroToMany', 'ERmandOne', 'exitX=0;exitY=0.5;entryX=1;entryY=0.5;')
    d.edge('log', 'appointments', 'booking_code (liên kết logic, không FK)', 'dashed=1;endArrow=none;'
           'exitX=0.3;exitY=1;entryX=1;entryY=0.3;')
    d.text('legend', 'Adapter chỉ đọc patients.phone (qua Appointment) và trả DeliveryResult; Decorator SMS ghi kết quả vào '
           'notification_log. Nhà cung cấp SMS là hệ thống ngoài, không có bảng.\n'
           'Ký hiệu crow\'s foot: ┤├ đúng một · o┤ không hoặc một · o< không hoặc nhiều. Viền đậm = bảng mới.',
           60, 730, 1880, 70, 15)
    d.save('adapter-erd.drawio')


def dfd():
    d = Diagram('Adapter — DFD', 2100, 880, OUT)
    d.title('MẪU ADAPTER — DFD MỨC 1: TỪ BẢN TIN ĐẾN NHÀ CUNG CẤP SMS', 2000)
    d.box('dec', 'Decorator SMS\n(chuỗi thông báo)', 40, 320, 250, 130, EXTERNAL)
    d.box('vendor', 'Nhà cung cấp SMS\n(eSMS) → Bệnh nhân', 1780, 320, 280, 130, EXTERNAL)
    d.box('p1', '1.0\nKiểm tra &\nchuẩn hóa số', 420, 290, 230, 190, PROCESS)
    d.box('p2', '2.0\nSoạn nội dung\nSMS', 850, 290, 230, 190, PROCESS)
    d.box('p3', '3.0\nGửi & dịch\nmã trạng thái', 1290, 290, 230, 190, PROCESS)
    d.box('d1', 'D1   patients (phone)', 380, 640, 320, 56, STORE)
    d.edge('dec', 'p1', 'số điện thoại\n+ bản tin', 'exitX=1;exitY=0.4;entryX=0;entryY=0.4;')
    d.edge('d1', 'p1', 'phone', 'exitX=0.5;exitY=0;entryX=0.4;entryY=1;')
    d.edge('p1', 'dec', 'SKIPPED_NO_PHONE /\nFAILED (số sai)', 'dashed=1;exitX=0.3;exitY=0.05;entryX=0.5;entryY=0;',
           [(489, 220), (165, 220)])
    d.edge('p1', 'p2', '+84xxxxxxxxx\n+ bản tin', 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')
    d.edge('p2', 'p3', 'msisdn + text', 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')
    d.edge('p3', 'vendor', 'submitSms(msisdn, text)', 'exitX=1;exitY=0.35;entryX=0;entryY=0.35;')
    d.edge('vendor', 'p3', 'mã 0 / 400 (tắt: -1)', 'exitX=0;exitY=0.7;entryX=1;entryY=0.7;')
    d.edge('p3', 'dec', 'DeliveryResult (SENT / FAILED)', 'exitX=0.5;exitY=0;entryX=0.15;entryY=0;',
           [(1405, 160), (78, 160)])
    d.text('note', 'Yourdon/DeMarco. Adapter không ghi kho nào; Decorator SMS nhận DeliveryResult và ghi notification_log.',
           40, 780, 2000, 50, 14)
    d.save('adapter-dfd.drawio')


def use_case():
    d = Diagram('Adapter — Use Case', 1800, 940, OUT)
    d.title('MẪU ADAPTER — KÊNH SMS CỦA NHÀ CUNG CẤP — SƠ ĐỒ USE CASE', 1700)
    d.system('Hệ thống đặt lịch khám — Kênh SMS (Adapter)', 280, 80, 1260, 760)
    d.actor('patient', 'Bệnh nhân', 120, 200)
    d.actor('ops', 'Người vận hành', 120, 650)
    d.actor('vendor', 'Nhà cung cấp SMS\n(eSMS)', 1640, 470)
    d.case('uc-receive', 'Nhận SMS thông báo lịch hẹn', 330, 180)
    d.case('uc-toggle', 'Bật / tắt kênh SMS\n(SMS_ENABLED)', 330, 640)
    d.case('uc-send', 'Gửi SMS qua kênh chung\n(NotificationChannel)', 760, 300)
    d.case('uc-phone', 'Chuẩn hóa số sang +84', 1170, 140)
    d.case('uc-text', 'Soạn nội dung SMS ngắn', 1170, 300)
    d.case('uc-submit', 'Gọi API nhà cung cấp\nvà dịch mã trả về', 1170, 460)
    d.case('uc-nophone', 'Bỏ qua khi bệnh nhân\nkhông có số', 330, 420)
    d.case('uc-disabled', 'Bỏ qua khi SMS đang tắt', 760, 690)
    d.assoc('patient', 'uc-receive')
    d.assoc('ops', 'uc-toggle')
    d.assoc('uc-submit', 'vendor')
    d.include('uc-receive', 'uc-send')
    for part in ('uc-phone', 'uc-text', 'uc-submit'):
        d.include('uc-send', part)
    d.extend('uc-nophone', 'uc-send', 'số rỗng')
    d.extend('uc-disabled', 'uc-send', 'SMS_ENABLED=false')
    d.text('scope', 'Phần còn lại của hệ thống chỉ biết "Gửi SMS qua kênh chung"; mọi chi tiết của nhà cung cấp nằm '
           'trong "Gọi API nhà cung cấp". Đổi nhà cung cấp = thay client phía sau, không đổi use case nào khác.',
           280, 855, 1260, 50, 14)
    d.save('adapter-use-case.drawio')


if __name__ == '__main__':
    class_diagram()
    sequence()
    erd()
    dfd()
    use_case()
    print('Generated Adapter class, sequence, ERD, DFD and use case in', OUT.relative_to(REPO))
