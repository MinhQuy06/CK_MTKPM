#!/usr/bin/env python3
"""Generate the overview diagrams of the Strategy feature: Use Case, ERD, DFD (level 0 and 1),
State Machine, Activity and Package. Monochrome (black on white), editable in draw.io."""
import math
from html import escape
from drawio import Diagram

TITLE = 21
STRAIGHT = 'edgeStyle=none;'
DASHED = 'dashed=1;'
NOTE = 'shape=note;size=18;whiteSpace=wrap;align=left;verticalAlign=top;fontSize=14;spacing=10;'


# ---------------------------------------------------------------- 1. Use Case
def use_case():
    d = Diagram('Strategy — Use Case', 1700, 1160)
    d.text('title', 'TÍNH PHÍ KHÁM THEO STRATEGY — SƠ ĐỒ USE CASE', 40, 15, 1600, 40, TITLE)
    d.box('system', 'Hệ thống đặt lịch khám — Tính phí khám theo Strategy', 280, 80, 1240, 980,
          'verticalAlign=top;fontSize=17;fontStyle=1;spacingTop=8;')
    actor = 'shape=umlActor;verticalLabelPosition=bottom;verticalAlign=top;fontSize=16;fontStyle=1;'
    d.box('patient', 'Bệnh nhân', 100, 560, 40, 80, actor)
    d.box('admin', 'Admin', 1590, 580, 40, 80, actor)
    cases = {
        'uc-quote': ('Xem báo giá theo loại phí\n(STANDARD / INSURANCE / VIP)', 330, 150),
        'uc-book': ('Đặt lịch khám\n(xác nhận giá đã báo)', 330, 300),
        'uc-reconfirm': ('Xác nhận lại giá mới', 330, 450),
        'uc-request': ('Gửi yêu cầu quyền hưởng\n(Bảo hiểm / VIP)', 330, 620),
        'uc-renew': ('Gia hạn quyền hưởng', 760, 860),
        'uc-status': ('Xem trạng thái và\nlịch sử quyền hưởng', 330, 800),
        'uc-price': ('Tính giá theo chiến lược\n(PricingContext → Strategy)', 760, 225),
        'uc-eligible': ('Kiểm tra quyền hưởng\ncòn hiệu lực', 760, 380),
        'uc-history': ('Ghi lịch sử quyết định\n(lý do, người quyết định)', 760, 690),
        'uc-list': ('Xem danh sách và\nchi tiết yêu cầu', 1150, 480),
        'uc-decide': ('Duyệt / từ chối\nyêu cầu quyền hưởng', 1150, 620),
        'uc-revoke': ('Thu hồi quyền hưởng\nđã duyệt', 1150, 760),
    }
    for key, (label, x, y) in cases.items():
        d.box(key, label, x, y, 320, 80, 'ellipse;whiteSpace=wrap;fontSize=15;')
    assoc = STRAIGHT + 'endArrow=none;'
    # Extension use cases (reconfirm, renew) inherit the actor through «extend».
    for key in ('uc-quote', 'uc-book', 'uc-request', 'uc-status'):
        d.edge('patient', key, '', assoc)
    for key in ('uc-list', 'uc-decide', 'uc-revoke'):
        d.edge('admin', key, '', assoc)
    rel = STRAIGHT + DASHED
    d.edge('uc-quote', 'uc-price', '«include»', rel)
    d.edge('uc-book', 'uc-price', '«include»', rel)
    d.edge('uc-eligible', 'uc-price', '«extend»\n[loại phí ≠ STANDARD]', rel)
    d.edge('uc-reconfirm', 'uc-book', '«extend»\n[409 PRICE_CHANGED]', rel)
    d.edge('uc-renew', 'uc-request', '«extend»\n[yêu cầu cũ APPROVED/REVOKED, cùng loại]', rel)
    d.edge('uc-decide', 'uc-history', '«include»', rel)
    d.edge('uc-revoke', 'uc-history', '«include»', rel)
    d.text('scope', 'Phạm vi: chỉ các ca sử dụng của tính năng tính phí khám theo Strategy. Bác sĩ không tương tác trực tiếp; '
           'giá gốc lấy từ doctors.consultation_fee.', 280, 1075, 1240, 50, 14)
    d.save('strategy-use-case.drawio')


# ---------------------------------------------------------------- 2. ERD
def table(d, key, title, rows, x, y, w=480, new=False):
    cells = ''.join(f'<tr><td style="padding:0 10px 0 0">{escape(tag)}</td><td style="padding:0 14px 0 0">{escape(col)}</td>'
                    f'<td>{escape(kind)}</td></tr>' for tag, col, kind in rows)
    h = 44 + 19 * len(rows) + 28
    d.box(key, title, x, y, w, h, 'swimlane;startSize=44;fontSize=16;fontStyle=1;' + ('strokeWidth=3;' if new else ''))
    d.box(key + '-body', f'<table style="border-collapse:collapse">{cells}</table>', 0, 44, w, h - 44,
          'shape=text;html=1;strokeColor=none;fillColor=none;whiteSpace=wrap;align=left;verticalAlign=top;'
          'fontSize=14;fontFamily=Courier New;spacing=10;', key)
    return y + h


def erd():
    d = Diagram('Strategy — ERD', 2360, 1300)
    d.text('title', 'TÍNH PHÍ KHÁM THEO STRATEGY — ERD (MỨC VẬT LÝ, KÝ HIỆU CROW\'S FOOT)', 40, 15, 2200, 40, TITLE)
    bottoms = [
        table(d, 'users', 'users', [('PK', 'id', 'BIGINT'), ('', 'email', 'VARCHAR(255) UK'), ('', 'role', 'VARCHAR(20)'),
                                    ('', 'is_active', 'BOOLEAN'), ('', '…', '')], 60, 140),
        table(d, 'patients', 'patients', [('PK,FK', 'user_id', 'BIGINT'), ('', 'full_name', 'VARCHAR(255)'),
                                          ('', 'phone', 'VARCHAR(20) UK'), ('', '…', '')], 60, 560),
        table(d, 'decisions', 'benefit_decisions  «new»', [
            ('PK', 'id', 'BIGINT'), ('FK', 'request_id', 'BIGINT NOT NULL'), ('', 'action', 'VARCHAR(20)'),
            ('', '', '(APPROVE | REJECT | REVOKE)'), ('', 'reason', 'VARCHAR(1000)'), ('FK', 'decided_by', 'BIGINT NOT NULL'),
            ('', 'decided_at', 'DATETIME(6)'), ('', '', ''), ('IDX', 'idx_decision_request', ''), ('', '  (request_id, id)', '')],
            640, 140, new=True),
        table(d, 'requests', 'benefit_requests  «new»', [
            ('PK', 'id', 'BIGINT'), ('FK', 'patient_id', 'BIGINT NOT NULL'), ('', 'kind', 'VARCHAR(20)'),
            ('', '', '(INSURANCE | VIP)'), ('', 'reference_code', 'VARCHAR(120)'), ('', 'requested_from', 'DATE'),
            ('', 'requested_until', 'DATE'), ('', 'status', 'VARCHAR(20)'), ('', '', "DEFAULT 'PENDING'"),
            ('', 'approved_from', 'DATE NULL'), ('', 'approved_until', 'DATE NULL'), ('FK', 'renewal_of_id', 'BIGINT NULL'),
            ('', 'created_at', 'DATETIME(6)'), ('', 'version', 'BIGINT (khóa lạc quan)'), ('', '', ''),
            ('IDX', 'idx_benefit_patient', ''), ('', '  (patient_id, created_at)', '')], 640, 560, new=True),
        table(d, 'appointments', 'appointments', [
            ('PK', 'id', 'BIGINT'), ('', 'booking_code', 'VARCHAR(100) UK'), ('FK', 'patient_id', 'BIGINT'),
            ('FK', 'doctor_id', 'BIGINT'), ('FK', 'schedule_id', 'BIGINT UK'), ('', 'status', 'VARCHAR(20)'),
            ('', 'price', 'DECIMAL(12,2) giá cuối'), ('', '…', ''), ('', 'pricing_key', 'VARCHAR(20)    «new»'),
            ('', 'base_price', 'DECIMAL(12,2)  «new»'), ('', 'discount_rate', 'DECIMAL(19,18) «new»'),
            ('FK', 'benefit_request_id', 'BIGINT NULL    «new»')], 1220, 560),
        table(d, 'doctors', 'doctors', [('PK,FK', 'user_id', 'BIGINT'), ('', 'full_name', 'VARCHAR(255)'),
                                        ('', 'consultation_fee', 'DECIMAL(12,2)'), ('', '', '= giá gốc'),
                                        ('', 'is_approved', 'BOOLEAN'), ('', '…', '')], 1800, 140),
        table(d, 'schedules', 'schedules', [('PK', 'id', 'BIGINT'), ('FK', 'doctor_id', 'BIGINT'), ('', 'work_date', 'DATE'),
                                            ('', 'start_time', 'TIME'), ('', 'end_time', 'TIME'),
                                            ('', 'is_available', 'BOOLEAN')], 1800, 560),
    ]
    requests_bottom = bottoms[3]
    low = max(bottoms) + 90

    def rel(src, dst, label, many, one, style='', points=()):
        d.edge(src, dst, label, f'startArrow={many};endArrow={one};startFill=0;endFill=0;' + style, points)

    rel('patients', 'users', 'user_id (1–1)', 'ERzeroToOne', 'ERmandOne', 'exitX=0.5;exitY=0;entryX=0.5;entryY=1;')
    rel('doctors', 'users', 'user_id (1–1)', 'ERzeroToOne', 'ERmandOne', 'exitX=0.5;exitY=0;entryX=0.5;entryY=0;',
        [(2040, 100), (300, 100)])
    rel('requests', 'patients', 'patient_id', 'ERzeroToMany', 'ERmandOne', 'exitX=0;exitY=0.15;entryX=1;entryY=0.5;')
    rel('decisions', 'requests', 'request_id', 'ERzeroToMany', 'ERmandOne', 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
    rel('decisions', 'users', 'decided_by', 'ERzeroToMany', 'ERmandOne', 'exitX=0;exitY=0.4;entryX=1;entryY=0.4;')
    rel('requests', 'requests', 'renewal_of_id', 'ERzeroToMany', 'ERzeroToOne',
        'exitX=0.3;exitY=1;entryX=0.7;entryY=1;', [(784, requests_bottom + 40), (976, requests_bottom + 40)])
    rel('appointments', 'requests', 'benefit_\nrequest_id', 'ERzeroToMany', 'ERzeroToOne',
        'exitX=0;exitY=0.78;entryX=1;entryY=0.6;', [(1165, 800)])
    rel('appointments', 'patients', 'patient_id', 'ERzeroToMany', 'ERmandOne',
        'exitX=0.5;exitY=1;entryX=0.5;entryY=1;', [(1460, low), (300, low)])
    rel('appointments', 'doctors', 'doctor_id', 'ERzeroToMany', 'ERmandOne',
        'exitX=0.5;exitY=0;entryX=0;entryY=0.5;', [(1460, 280)])
    rel('appointments', 'schedules', 'schedule_id', 'ERzeroToOne', 'ERmandOne',
        'exitX=1;exitY=0.2;entryX=0;entryY=0.35;')
    rel('schedules', 'doctors', 'doctor_id', 'ERzeroToMany', 'ERmandOne', 'exitX=0.5;exitY=0;entryX=0.5;entryY=1;')
    d.text('legend',
           'Ký hiệu: ┤├ đúng một · o┤ không hoặc một · o< không hoặc nhiều. Viền đậm và «new»: bảng/cột thêm bởi '
           'db/upgrade/strategy 001–005.\nBảng dùng chung chỉ vẽ cột liên quan (…). appointments lưu snapshot giá lúc đặt: '
           'pricing_key, base_price, discount_rate, price = giá cuối; benefit_request_id trỏ quyền hưởng đã dùng.',
           60, low + 30, 2240, 70, 15)
    d.save('strategy-erd.drawio')


# ---------------------------------------------------------------- 3. DFD
EXTERNAL = 'whiteSpace=wrap;fontSize=17;fontStyle=1;'
PROCESS = 'ellipse;whiteSpace=wrap;fontSize=15;fontStyle=1;'
STORE = 'shape=partialRectangle;right=0;whiteSpace=wrap;align=left;spacingLeft=14;fontSize=15;fontStyle=1;'


def dfd_level0():
    d = Diagram('Strategy — DFD mức 0', 1960, 720)
    d.text('title', 'TÍNH PHÍ KHÁM THEO STRATEGY — DFD MỨC 0 (SƠ ĐỒ NGỮ CẢNH)', 40, 15, 1880, 40, TITLE)
    cx, cy, r = 970, 350, 210
    d.box('patient', 'Bệnh nhân', 40, 120, 260, 460, EXTERNAL)
    d.box('admin', 'Admin', 1640, 200, 260, 300, EXTERNAL)
    d.box('system', '0\n\nHệ thống tính phí khám\n(Strategy)', cx - r, cy - r, 2 * r, 2 * r, PROCESS + 'fontSize=18;')

    def edge_x(y, side):
        return cx + side * math.sqrt(r * r - (y - cy) ** 2)

    left = [(340, True, 'Yêu cầu báo giá (lịch khám, loại phí)'),
            (410, False, 'Báo giá (giá gốc, tỉ lệ giảm, giá cuối)'),
            (490, True, 'Yêu cầu quyền hưởng / gia hạn (loại, mã, thời hạn)'),
            (560, False, 'Trạng thái và lịch sử quyền hưởng'),
            (640, True, 'Yêu cầu đặt lịch + giá kỳ vọng'),
            (710, False, 'Lịch hẹn CONFIRMED · 409 PRICE_CHANGED · 403')]
    for y, inbound, label in left:
        y -= 180
        a, b = (300, edge_x(y, -1)) if inbound else (edge_x(y, -1), 300)
        d.line(a, y, b, y, label)
    right = [(290, True, 'Quyết định duyệt / từ chối / thu hồi + lý do'),
             (410, False, 'Danh sách, chi tiết, lịch sử yêu cầu')]
    for y, inbound, label in right:
        a, b = (1640, edge_x(y, 1)) if inbound else (edge_x(y, 1), 1640)
        d.line(a, y, b, y, label)
    d.text('note', 'Ký hiệu Yourdon/DeMarco: hình chữ nhật = tác nhân ngoài, hình tròn = tiến trình, mũi tên = luồng dữ liệu. '
           'Chi tiết kho dữ liệu ở DFD mức 1.', 40, 620, 1880, 50, 14)
    d.save('strategy-dfd-level0.drawio')


def dfd_level1():
    d = Diagram('Strategy — DFD mức 1', 2340, 1080)
    d.text('title', 'TÍNH PHÍ KHÁM THEO STRATEGY — DFD MỨC 1', 40, 15, 2260, 40, TITLE)
    d.box('patient', 'Bệnh nhân', 150, 90, 1700, 90, EXTERNAL)
    d.box('admin', 'Admin', 2080, 420, 220, 120, EXTERNAL)
    r = 130
    procs = {'p1': ('1.0\nBáo giá\nphí khám', 400), 'p3': ('3.0\nĐặt lịch và\nchốt giá', 1050),
             'p2': ('2.0\nQuản lý\nquyền hưởng', 1700)}
    for key, (label, cx) in procs.items():
        d.box(key, label, cx - r, 350, 2 * r, 2 * r, PROCESS)
        off = 75
        top = 480 - math.sqrt(r * r - off * off)
        down, up = {'p1': ('scheduleId,\nloại phí', 'báo giá (gốc,\ngiảm, cuối)'),
                    'p3': ('đặt lịch +\ngiá kỳ vọng', 'CONFIRMED /\n409 / 403'),
                    'p2': ('yêu cầu /\ngia hạn', 'trạng thái\nquyền hưởng')}[key]
        d.line(cx - off, 180, cx - off, top, down)
        d.line(cx + off, top, cx + off, 180, up)
    for y, label, inbound in ((450, 'quyết định + lý do', True), (510, 'danh sách, chi tiết, lịch sử', False)):
        x = 1700 + math.sqrt(r * r - (y - 480) ** 2)
        a, b = (2080, x) if inbound else (x, 2080)
        d.line(a, y, b, y, label)
    d.box('d4', 'D4   doctors · schedules', 560, 800, 340, 56, STORE)
    d.box('d3', 'D3   appointments', 960, 800, 320, 56, STORE)
    d.box('d1', 'D1   benefit_requests', 1380, 800, 340, 56, STORE)
    d.box('d2', 'D2   benefit_decisions', 1800, 800, 360, 56, STORE)

    def flow(src, dst, label, ex, ey, nx, ny, points=()):
        style = (STRAIGHT if not points else '') + f'exitX={ex};exitY={ey};entryX={nx};entryY={ny};fontSize=13;'
        d.edge(src, dst, label, style, points)

    flow('d4', 'p1', 'giá gốc, ngày khám,\ntrạng thái slot', 0.15, 0, 0.8, 0.95)
    flow('d3', 'p1', 'slot đã có lịch?', 0.5, 1, 0.27, 0.95, [(1120, 915), (340, 915)])
    flow('d1', 'p1', 'quyền hưởng APPROVED còn hiệu lực (hôm nay + ngày khám)', 0.5, 1, 0.5, 1,
         [(1550, 960), (400, 960)])
    flow('d4', 'p3', 'giá gốc, slot\n(khóa ghi)', 0.75, 0, 0.0, 0.7)
    flow('p3', 'd4', 'is_available\n= false', 0.2, 0.92, 0.98, 0)
    flow('d1', 'p3', 'quyền hưởng\n(khóa, ngày khám)', 0.12, 0, 0.92, 0.82)
    flow('p3', 'd3', 'lịch hẹn +\nsnapshot giá', 0.5, 1, 0.3, 0)
    flow('p2', 'd1', 'ghi yêu cầu,\ntrạng thái', 0.12, 0.8, 0.55, 0)
    flow('d1', 'p2', 'đọc yêu cầu', 0.95, 0, 0.62, 1)
    flow('p2', 'd2', 'quyết định (action,\nlý do, người duyệt)', 0.88, 0.82, 0.15, 0)
    flow('d2', 'p2', 'lịch sử\nquyết định', 0.6, 0, 1, 0.62)
    d.text('note', 'D1–D2: bảng mới của tính năng. D3: appointments có thêm cột snapshot giá (pricing_key, base_price, '
           'discount_rate, benefit_request_id). D4: giá gốc doctors.consultation_fee và lịch trống schedules.',
           40, 1000, 2260, 50, 14)
    d.save('strategy-dfd-level1.drawio')


# ---------------------------------------------------------------- 4. State Machine
STATE = 'rounded=1;arcSize=25;whiteSpace=wrap;fontSize=15;fontStyle=1;'
INITIAL = 'ellipse;fillColor=#000000;'
FINAL = 'shape=endState;fillColor=#000000;'


def state_machine():
    d = Diagram('Strategy — State Machine', 2160, 1060)
    d.text('title', 'BENEFITREQUEST — SƠ ĐỒ TRẠNG THÁI (STATE MACHINE)', 40, 15, 2080, 40, TITLE)
    d.box('init', '', 40, 335, 30, 30, INITIAL)
    d.box('pending', 'PENDING\n(chờ Admin xử lý)', 260, 305, 220, 90, STATE)
    d.box('approved', 'APPROVED  (status lưu trong DB)', 720, 110, 1000, 520,
          'swimlane;rounded=1;arcSize=4;startSize=40;fontSize=16;fontStyle=1;')
    d.box('a-init', '', 485, 60, 30, 30, INITIAL, 'approved')
    d.box('a-choice', '', 470, 130, 60, 60, 'rhombus;', 'approved')
    d.box('a-wait', 'NOT_YET_VALID\n(chưa tới ngày hiệu lực)', 40, 330, 220, 90, STATE, 'approved')
    d.box('a-active', 'Đang hiệu lực\n(dùng được khi đặt lịch)', 390, 330, 220, 90, STATE, 'approved')
    d.box('a-expired', 'EXPIRED\n(hết hạn)', 740, 330, 220, 90, STATE, 'approved')
    d.box('a-note', 'Trạng thái con được suy ra theo ngày hiện tại (Asia/Ho_Chi_Minh) khi hiển thị; '
          'DB vẫn lưu status = APPROVED.', 30, 450, 940, 50,
          'shape=text;strokeColor=none;fillColor=none;whiteSpace=wrap;align=left;fontSize=14;fontStyle=2;', 'approved')
    d.box('rejected', 'REJECTED\n(bị từ chối)', 260, 760, 220, 90, STATE)
    d.box('revoked', 'REVOKED\n(bị thu hồi)', 1100, 760, 240, 90, STATE)
    d.box('end-rejected', '', 355, 940, 30, 30, FINAL)
    d.box('end-revoked', '', 1205, 940, 30, 30, FINAL)

    d.edge('init', 'pending', 'gửi yêu cầu\n/ gia hạn', 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')
    d.edge('pending', 'approved', 'Admin duyệt [APPROVE]\n/ đặt approved_from..until,\nghi BenefitDecision',
           'exitX=1;exitY=0.5;entryX=0;entryY=0.46;')
    d.edge('pending', 'rejected', 'Admin từ chối [REJECT]\n/ ghi BenefitDecision (lý do)', 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
    d.edge('approved', 'revoked', 'Admin thu hồi [REVOKE]\n/ ghi BenefitDecision (lý do)', 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
    d.edge('rejected', 'end-rejected', '', 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
    d.edge('revoked', 'end-revoked', '', 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
    d.edge('a-init', 'a-choice', '', 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
    d.edge('a-choice', 'a-wait', '[today < approved_from]', 'exitX=0;exitY=0.5;entryX=0.5;entryY=0;')
    d.edge('a-choice', 'a-active', '[from ≤ today ≤ until]', 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
    d.edge('a-choice', 'a-expired', '[today > approved_until]', 'exitX=1;exitY=0.5;entryX=0.5;entryY=0;')
    d.edge('a-wait', 'a-active', 'when(today ≥\napproved_from)', 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')
    d.edge('a-active', 'a-expired', 'when(today >\napproved_until)', 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')

    d.box('n-renew', 'Gia hạn: khi yêu cầu cũ đang APPROVED hoặc REVOKED, bệnh nhân gửi yêu cầu MỚI cùng loại (kind). '
          'Yêu cầu mới có renewal_of_id = id cũ và bắt đầu ở PENDING; yêu cầu cũ giữ nguyên trạng thái.',
          1770, 110, 360, 130, NOTE)
    d.box('n-lock', 'Mỗi quyết định của Admin khóa bản ghi và kiểm tra trạng thái nguồn (PENDING cho duyệt/từ chối, '
          'APPROVED cho thu hồi). Sai trạng thái → 409 "vui lòng tải lại". Cột version chống ghi đè đồng thời.',
          1770, 290, 360, 150, NOTE)
    d.box('n-invalid', 'INVALID (APPROVED nhưng thiếu approved_from/until) chỉ là chốt chặn dữ liệu lỗi khi hiển thị, '
          'không phải trạng thái nghiệp vụ nên không vẽ.', 1770, 490, 360, 110, NOTE)
    d.box('n-booking', 'Báo giá và đặt lịch INSURANCE/VIP chỉ chấp nhận yêu cầu APPROVED có khoảng '
          '[approved_from, approved_until] bao phủ ngày khám.', 1770, 650, 360, 110, NOTE)
    d.save('strategy-state-machine.drawio')


# ---------------------------------------------------------------- 5. Activity
ACTION = 'rounded=1;arcSize=30;whiteSpace=wrap;fontSize=15;'
DECISION = 'rhombus;whiteSpace=wrap;fontSize=14;'
CONNECTOR = 'ellipse;fontSize=16;fontStyle=1;'


def activity():
    d = Diagram('Strategy — Activity', 1700, 2010)
    d.text('title', 'ĐẶT LỊCH CÓ TÍNH GIÁ THEO STRATEGY — SƠ ĐỒ HOẠT ĐỘNG (ACTIVITY)', 30, 10, 1640, 40, TITLE)
    lane = 'swimlane;startSize=44;fontSize=17;fontStyle=1;'
    d.box('lane-patient', 'Bệnh nhân', 30, 60, 620, 1900, lane)
    d.box('lane-system', 'Hệ thống', 650, 60, 1020, 1900, lane)

    def act(key, label, lane_side, y, h=70):
        x = 150 if lane_side == 'p' else 790
        d.box(key, label, x, y, 380 if lane_side == 'p' else 400, h, ACTION)

    def decide(key, label, y):
        d.box(key, label, 860, y, 260, 120, DECISION)

    def error(key, label, y):
        d.box(key, label, 1250, y, 390, 80, ACTION)

    d.box('start', '', 325, 120, 30, 30, INITIAL)
    act('a1', 'Chọn bác sĩ và khung giờ trống', 'p', 170)
    act('s1', 'Tải loại phí khả dụng\n(GET /appointments/pricing-options)', 's', 270)
    act('a2', 'Chọn loại phí\n(STANDARD / INSURANCE / VIP)', 'p', 370)
    act('s2', 'Báo giá: PricingContext chọn Strategy,\ncalculate(consultation_fee)  (GET /quote)', 's', 470, 80)
    decide('d1', 'đủ quyền hưởng?', 590)
    error('err1', '403: chưa có quyền hưởng hợp lệ\ncho ngày khám', 610)
    act('a3', 'Xem báo giá, bấm Xác nhận\n(gửi giá kỳ vọng + ngày giờ ca)', 'p', 740, 80)
    act('s3', 'POST /appointments: mở giao dịch,\nkhóa slot (FOR UPDATE)', 's', 860, 80)
    decide('d2', 'slot còn trống,\nkhớp ngày giờ?', 980)
    error('err2', '409 SLOT_UNAVAILABLE:\nbỏ chọn, tải lại khung giờ', 1000)
    act('s4', 'Nếu loại ≠ STANDARD: khóa và kiểm tra\nquyền hưởng (lockedEligible)', 's', 1130, 80)
    decide('d3', 'quyền hưởng\ncòn hiệu lực?', 1250)
    error('err3', '403: quyền hưởng không còn hợp lệ\ncho ngày khám', 1270)
    act('s5', 'Tính lại giá bằng Strategy hiện hành\n(PricingContext.calculate)', 's', 1400, 80)
    decide('d4', 'giá cuối, giá gốc, tỉ lệ\nkhớp giá kỳ vọng?', 1520)
    act('a4', 'Xem giá mới và xác nhận lại', 'p', 1545, 70)
    act('s6', 'Lưu Appointment CONFIRMED + snapshot giá\n(pricing_key, base_price, discount_rate,\n'
        'benefit_request_id); is_available = false', 's', 1680, 90)
    act('s7', 'Gửi email xác nhận (bất đồng bộ)', 's', 1820, 70)
    act('a5', 'Nhận mã đặt lịch\nvà giá đã chốt', 'p', 1820, 70)
    d.box('end', '', 325, 1915, 30, 30, FINAL)
    # Connectors: same letter = same point in the flow.
    d.box('ca-out1', 'A', 1425, 720, 40, 40, CONNECTOR)
    d.box('ca-out2', 'A', 1425, 1380, 40, 40, CONNECTOR)
    d.box('ca-in', 'A', 70, 385, 40, 40, CONNECTOR)
    d.box('cb-out', 'B', 1425, 1110, 40, 40, CONNECTOR)
    d.box('cb-in', 'B', 70, 185, 40, 40, CONNECTOR)
    d.box('cc-out', 'C', 70, 1560, 40, 40, CONNECTOR)
    d.box('cc-in', 'C', 710, 880, 40, 40, CONNECTOR)

    down = 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;'
    right_to_top = 'exitX=1;exitY=0.5;entryX=0.5;entryY=0;'
    left_to_top = 'exitX=0;exitY=0.5;entryX=0.5;entryY=0;'
    side = 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;'
    back = 'exitX=0;exitY=0.5;entryX=1;entryY=0.5;'
    d.edge('start', 'a1', '', down)
    d.edge('a1', 's1', '', right_to_top)
    d.edge('s1', 'a2', '', left_to_top)
    d.edge('a2', 's2', '', right_to_top)
    d.edge('s2', 'd1', '', down)
    d.edge('d1', 'err1', '[không]', side)
    d.edge('err1', 'ca-out1', '', down)
    d.edge('ca-in', 'a2', '', side)
    d.edge('d1', 'a3', '[có]', left_to_top)
    d.edge('a3', 's3', '', right_to_top)
    d.edge('s3', 'd2', '', down)
    d.edge('d2', 'err2', '[không]', side)
    d.edge('err2', 'cb-out', '', down)
    d.edge('cb-in', 'a1', '', side)
    d.edge('d2', 's4', '[có]', down)
    d.edge('s4', 'd3', '', down)
    d.edge('d3', 'err3', '[không]', side)
    d.edge('err3', 'ca-out2', '', down)
    d.edge('d3', 's5', '[STANDARD hoặc hợp lệ]', down)
    d.edge('s5', 'd4', '', down)
    d.edge('d4', 'a4', '[không] 409 PRICE_CHANGED (kèm giá mới)', back)
    d.edge('a4', 'cc-out', '', back)
    d.edge('cc-in', 's3', '', side)
    d.edge('d4', 's6', '[khớp]', down)
    d.edge('s6', 's7', '', down)
    d.edge('s7', 'a5', '', back)
    d.edge('a5', 'end', '', down)
    d.text('note', 'Nút tròn có chữ (A, B, C) là connector: luồng đi vào connector tiếp tục ở connector cùng chữ. '
           'Loại phí không hỗ trợ trả 400 (không vẽ). Toàn bộ bước khóa, kiểm tra và lưu nằm trong một giao dịch.',
           30, 1965, 1640, 40, 14)
    d.save('strategy-activity.drawio')


# ---------------------------------------------------------------- 6. Package
def package():
    d = Diagram('Strategy — Package', 1940, 1150)
    d.text('title', 'TÍNH PHÍ KHÁM THEO STRATEGY — SƠ ĐỒ GÓI (PACKAGE)', 30, 10, 1880, 40, TITLE)

    def pkg(key, name, lines, x, y, w, h, extra='', tab=170):
        body = '<br>'.join(lines)
        d.box(key, f'<b>{name}</b><br>{body}', x, y, w, h,
              f'shape=folder;tabWidth={tab};tabHeight=28;tabPosition=left;html=1;whiteSpace=wrap;align=left;'
              f'verticalAlign=top;spacingLeft=12;spacingTop=30;fontSize=14;' + extra)

    pkg('strategy', 'com.datlichkhambenh.backend.strategy', [], 30, 70, 1290, 1020, 'fontSize=16;', 420)
    pkg('controller', 'controller', ['ConsultationQuoteController', 'PatientBenefitController',
                                     'AdminBenefitController'], 70, 160, 380, 190)
    pkg('dto', 'dto', ['request: ConsultationQuoteRequest, SubmitBenefitRequest,',
                       '&nbsp;&nbsp;BenefitDecisionInput, BenefitRevokeInput',
                       'response: ConsultationQuoteDto, BenefitRequestDto,', '&nbsp;&nbsp;BenefitDecisionDto'],
        510, 160, 770, 190)
    pkg('service', 'service', ['ConsultationQuoteService «Client»', 'BenefitService',
                               '&nbsp;&nbsp;(eligible / lockedEligible,', '&nbsp;&nbsp;duyệt, từ chối, thu hồi)'],
        70, 440, 380, 210)
    pkg('pricing', 'pricing «Strategy pattern»', [
        '«interface» ConsultationPricingStrategy — <i>Strategy</i>',
        'PricingContext — <i>Context</i> (registry theo key, làm tròn HALF_UP)',
        'StandardPricingStrategy (tỉ lệ 0) — <i>ConcreteStrategy</i>',
        'InsurancePricingStrategy (tỉ lệ 0.8) — <i>ConcreteStrategy</i>',
        'VipPricingStrategy (tỉ lệ 0.1) — <i>ConcreteStrategy</i>',
        'PricingAmounts, PricingChoice — giá trị trả về'], 510, 440, 770, 240, 'strokeWidth=3;', 260)
    pkg('repository', 'repository', ['BenefitRequestRepository', 'BenefitDecisionRepository'], 70, 780, 380, 170)
    pkg('entity', 'entity', ['BenefitRequest', 'BenefitDecision'], 510, 800, 380, 150)
    d.box('roles', 'Vai trò trong mẫu Strategy:\nClient: ConsultationQuoteService, BookingServiceImpl\n'
          'Context: PricingContext\nStrategy: ConsultationPricingStrategy\nConcreteStrategy: Standard / Insurance / Vip',
          930, 790, 370, 170, NOTE)

    pkg('shared', 'com.datlichkhambenh.backend (dùng chung)', [], 1380, 70, 530, 1020, 'dashed=1;fontSize=16;', 400)
    pkg('booking', 'service.impl', ['BookingServiceImpl «Client»', '(tính giá + lockedEligible khi đặt lịch)'],
        1420, 160, 450, 150)
    pkg('shared-repo', 'repository', ['ScheduleRepository, AppointmentRepository,', 'PatientRepository, DoctorRepository'],
        1420, 400, 450, 160)
    pkg('shared-entity', 'entity', ['User, Patient, Doctor, Schedule,', 'Appointment (+ cột snapshot giá)'],
        1420, 630, 450, 160)
    pkg('exception', 'exception', ['BookingConflictException,', 'BookingRejectionException, AppException'],
        1420, 860, 450, 150)

    use = DASHED + 'fontSize=13;'
    d.edge('controller', 'service', '«use»', use + 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
    d.edge('controller', 'dto', '«use»', use + 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')
    d.edge('service', 'pricing', '«use»', use + 'exitX=1;exitY=0.5;entryX=0;entryY=0.4;')
    d.edge('service', 'repository', '«use»', use + 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
    d.edge('repository', 'entity', '«use»', use + 'exitX=1;exitY=0.55;entryX=0;entryY=0.5;')
    d.edge('booking', 'pricing', '«use»', use + 'exitX=0;exitY=0.4;entryX=1;entryY=0.3;',
           [(1340, 220), (1340, 512)])
    d.edge('booking', 'service', '«use»', use + 'exitX=0;exitY=0.8;entryX=0.8;entryY=0;',
           [(1358, 280), (1358, 400), (374, 400)])
    d.edge('strategy', 'shared', '«use»', use + 'exitX=1;exitY=0.97;entryX=0;entryY=0.97;')
    d.save('strategy-package.drawio')


if __name__ == '__main__':
    use_case(); erd(); dfd_level0(); dfd_level1(); state_machine(); activity(); package()
    print('Generated 7 editable overview diagrams (Use Case, ERD, DFD 0/1, State Machine, Activity, Package).')
