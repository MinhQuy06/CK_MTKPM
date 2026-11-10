#!/usr/bin/env python3
"""Overview map: where the 7 design patterns sit and how a booking flows through them."""
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / 'common'))
from drawio_kit import Diagram, REPO, DEPENDS  # noqa: E402

OUT = REPO / 'docs/diagrams'
BOX = 'whiteSpace=wrap;fontSize=15;'
ACTOR = 'whiteSpace=wrap;fontSize=16;fontStyle=1;rounded=1;'


def frame(d, key, label, x, y, w, h):
    d.box(key, '', x, y, w, h, 'fillColor=none;dashed=1;strokeWidth=2;')
    d.text(key + '-label', label, x + 10, y + 4, w - 20, 30, 16)


def main():
    d = Diagram('Bản đồ 7 mẫu', 2600, 1560, OUT)
    d.title('HỆ THỐNG ĐẶT LỊCH KHÁM — BẢN ĐỒ 7 MẪU THIẾT KẾ', 2500)

    # Tầng người dùng
    d.box('patient', 'Bệnh nhân', 120, 90, 260, 60, ACTOR)
    d.box('admin', 'Admin', 1200, 90, 260, 60, ACTOR)
    d.box('guest', 'Khách đăng ký', 2000, 90, 260, 60, ACTOR)

    # Tầng controller
    d.box('bookingctl', 'BookingController\n/api/v1/appointments', 60, 210, 380, 70, BOX)
    d.box('quotectl', 'ConsultationQuoteController\nPatient/AdminBenefitController', 560, 210, 420, 70, BOX)
    d.box('adminctl', 'AdminController\n/api/v1/admin/doctors|patients', 1120, 210, 420, 70, BOX)
    d.box('authctl', 'AuthController\n/api/v1/auth/register', 1940, 210, 380, 70, BOX)
    d.edge('patient', 'bookingctl', '', 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
    d.edge('patient', 'quotectl', 'báo giá, quyền hưởng', 'exitX=1;exitY=0.5;entryX=0.5;entryY=0;', [(770, 120)])
    d.edge('admin', 'adminctl', '', 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
    d.edge('guest', 'authctl', '', 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')

    # Facade
    frame(d, 'f-facade', '«Facade»', 40, 330, 420, 160)
    d.box('facade', 'BookingFacade\nconfirm · bookWithoutQuote · cancel\ndetails · myAppointments', 60, 370, 380, 100, BOX)
    d.edge('bookingctl', 'facade', '', 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')

    # Factory
    frame(d, 'f-factory', '«Factory Method»', 1120, 330, 1440, 250)
    d.box('registry', 'AccountFactories\nforRole(role)', 1150, 380, 320, 80, BOX)
    d.box('creator', 'AccountFactory  «abstract»\ncreate() → createProfile()', 1560, 370, 380, 90, BOX)
    d.box('pf', 'PatientAccountFactory', 1560, 490, 300, 60, BOX)
    d.box('df', 'DoctorAccountFactory', 1980, 490, 300, 60, BOX)
    d.edge('adminctl', 'registry', 'AdminManagementServiceImpl', 'exitX=0.3;exitY=1;entryX=0.3;entryY=0;')
    d.edge('authctl', 'registry', 'AuthServiceImpl.register', 'exitX=0.3;exitY=1;entryX=0.8;entryY=0;', [(2054, 345), (1406, 345)])
    d.edge('registry', 'creator', '', 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')
    d.edge('pf', 'creator', '', 'endArrow=block;endFill=0;endSize=14;exitX=0.5;exitY=0;entryX=0.4;entryY=1;')
    d.edge('df', 'creator', '', 'endArrow=block;endFill=0;endSize=14;exitX=0.5;exitY=0;entryX=0.9;entryY=1;')

    # Booking service + Strategy
    d.box('booking', 'BookingServiceImpl\nkhóa ca → kiểm tra quyền hưởng → tính giá\n→ lưu lịch hẹn → phát sự kiện', 40, 620, 460, 110, BOX)
    d.edge('facade', 'booking', 'createBooking / cancelBooking …', 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
    frame(d, 'f-strategy', '«Strategy»', 560, 330, 480, 400)
    d.box('context', 'PricingContext\ncalculate(key, fee)', 590, 370, 420, 80, BOX)
    d.box('strategies', 'ConsultationPricingStrategy\nStandard · Insurance · VIP', 590, 500, 420, 80, BOX)
    d.box('benefit', 'BenefitService\n(điều kiện dùng Insurance/VIP)', 590, 620, 420, 80, BOX)
    d.edge('quotectl', 'context', 'ConsultationQuoteService', 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
    d.edge('context', 'strategies', 'chọn theo key', 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
    d.edge('booking', 'context', 'tính giá', 'exitX=1;exitY=0.2;entryX=0;entryY=0.6;', [(530, 642), (530, 418)])
    d.edge('booking', 'benefit', 'khóa quyền hưởng', 'exitX=1;exitY=0.6;entryX=0;entryY=0.6;')

    # Observer
    frame(d, 'f-observer', '«Observer»', 40, 780, 1000, 230)
    d.box('events', 'ApplicationEventPublisher\nAppointmentBooked / Cancelled', 60, 830, 400, 80, BOX)
    d.box('emailobs', 'AppointmentEmailObserver', 560, 820, 420, 60, BOX)
    d.box('cacheobs', 'SlotCacheObserver → Redis', 560, 920, 420, 60, BOX)
    d.edge('booking', 'events', 'publishEvent', 'exitX=0.5;exitY=1;entryX=0.5;entryY=0;')
    d.edge('events', 'emailobs', '', 'exitX=1;exitY=0.3;entryX=0;entryY=0.5;')
    d.edge('events', 'cacheobs', '', 'exitX=1;exitY=0.8;entryX=0;entryY=0.5;')

    # Decorator
    frame(d, 'f-decorator', '«Decorator»  (bean @Primary AsyncEmailNotificationService)', 40, 1060, 1500, 170)
    chain = [('dedup', 'DeduplicationDecorator\nchống gửi trùng'), ('sms', 'SmsNotificationDecorator\ngửi thêm SMS'),
             ('audit', 'AuditLogDecorator\nghi lịch sử'), ('email', 'AsyncEmailNotification\nServiceImpl (gốc)')]
    for i, (key, label) in enumerate(chain):
        d.box(key, label, 60 + i * 370, 1110, 320, 90, BOX)
        if i:
            d.edge(chain[i - 1][0], key, 'inner', 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')
    d.edge('emailobs', 'dedup', 'sendBookingConfirmation …', 'exitX=1;exitY=0.5;entryX=0.5;entryY=0;', [(1010, 850), (1010, 1040), (220, 1040)])
    d.box('log', 'notification_log', 1600, 1130, 260, 56, 'shape=partialRectangle;right=0;whiteSpace=wrap;align=left;spacingLeft=14;fontSize=15;fontStyle=1;')
    d.edge('audit', 'log', '', DEPENDS + 'exitX=0.5;exitY=1;entryX=0;entryY=0.8;', [(930, 1240), (1580, 1240), (1580, 1175)])

    # Adapter
    frame(d, 'f-adapter', '«Adapter»', 40, 1290, 800, 170)
    d.box('channel', 'NotificationChannel\n«interface»', 60, 1340, 220, 90, BOX)
    d.box('adapter', 'SmsChannelAdapter\n+ VietnamPhone (+84)', 320, 1340, 240, 90, BOX)
    d.box('vendor', 'SimulatedSms\nGatewayClient', 600, 1340, 220, 90, BOX)
    d.edge('sms', 'channel', 'send(phone, message)', 'exitX=0.3;exitY=1;entryX=0.5;entryY=0;', [(526, 1270), (170, 1270)])
    d.edge('adapter', 'channel', '', 'dashed=1;endArrow=block;endFill=0;endSize=14;exitX=0;exitY=0.5;entryX=1;entryY=0.5;')
    d.edge('adapter', 'vendor', 'submitSms', 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')

    # Builder
    frame(d, 'f-builder', '«Builder»', 900, 1290, 1240, 170)
    d.box('director', 'NotificationMessageDirector\nbookingConfirmed · reminder · cancelled', 920, 1340, 420, 90, BOX)
    d.box('builder', 'NotificationMessageBuilder\n→ EmailNotificationMessage', 1400, 1340, 360, 90, BOX)
    d.box('rabbit', 'RabbitMQ\n(sau commit)', 1820, 1340, 280, 90, ACTOR)
    d.edge('email', 'director', '', 'exitX=0.5;exitY=1;entryX=0.8;entryY=0;', [(1270, 1270), (1256, 1270)])
    d.edge('director', 'builder', 'các bước dựng', 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')
    d.edge('builder', 'rabbit', 'bản tin', 'exitX=1;exitY=0.5;entryX=0;entryY=0.5;')

    d.text('legend', 'Khung nét đứt = một mẫu. Luồng đặt lịch: Bệnh nhân → BookingController → Facade → BookingServiceImpl '
           '(Strategy tính giá) → sự kiện (Observer) → chuỗi Decorator → SMS qua Adapter và email dựng bằng Builder. '
           'Tạo tài khoản đi riêng qua Factory Method.', 40, 1490, 2500, 50, 15)
    d.save('design-patterns-map.drawio')
    print('Generated overview map in', OUT.relative_to(REPO))


if __name__ == '__main__':
    main()
