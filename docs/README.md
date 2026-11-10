# Thiết kế lại hệ thống đặt lịch khám bằng 7 mẫu thiết kế

Hệ thống đặt lịch khám bệnh (Spring Boot + HTML/JS) được refactor để áp dụng 7 mẫu thiết kế GoF. Mỗi mẫu giải quyết một vấn đề có thật trong code đang chạy, nối vào đúng một luồng, có test chứng minh và 4 sơ đồ: class, sequence, ERD, DFD.

## Bản đồ 7 mẫu

Sẽ có: sơ đồ tổng thể cho thấy 7 mẫu nằm ở tầng nào và luồng đặt lịch đi qua chúng ra sao.

## Các mẫu

| Mẫu | Vấn đề trước khi áp dụng | Chỗ áp dụng | Thư mục |
|---|---|---|---|
| Strategy | Giá khám chỉ có một công thức; thêm Bảo hiểm/VIP phải sửa chỗ đặt lịch | Tính phí khám: Standard, Insurance, VIP | [strategy/](strategy/) |
| Builder | Bản tin email dựng bằng constructor 9 tham số String, dễ truyền nhầm, không kiểm tra trường bắt buộc | Dựng bản tin thông báo + Director cho đặt lịch/hủy/nhắc lịch | [builder/](builder/) |
| Observer | Dịch vụ đặt lịch gọi thẳng email và cache; thêm phản ứng mới phải sửa nó | Đặt/hủy lịch phát sự kiện; observer email và observer cache phản ứng | [observer/](observer/) |
| Adapter | Không có chỗ cắm kênh SMS của nhà cung cấp có giao diện khác | Client SMS giả lập được bọc thành `NotificationChannel` | [adapter/](adapter/) |
| Decorator | Không có lịch sử thông báo, không chống gửi trùng, chỉ có email | Bọc dịch vụ thông báo: chống trùng → SMS → ghi lịch sử → email | [decorator/](decorator/) |
| Factory Method | Tạo tài khoản Bệnh nhân/Bác sĩ lặp ở đăng ký và Admin | `AccountFactory` theo vai trò, dùng chung cho đăng ký và Admin | [factory/](factory/) |
| Facade | Controller đặt lịch phải biết nhiều dịch vụ bên dưới | `BookingFacade` là cửa duy nhất của `BookingController` | [facade/](facade/) |

Thư mục README và kịch bản của 6 mẫu mới: sẽ có (đang làm). Sơ đồ đã có trong `docs/<mẫu>/diagrams/`.

## Thành viên

| Tên | Mẫu | Ghi chú |
|---|---|---|
| | | |
| | | |
| | | |
| | | |

## Sinh lại sơ đồ

```bash
python3 scripts/<mẫu>/generate-diagrams.py
DRAWIO_BIN=/path/to/draw.io bash scripts/common/export-diagrams.sh docs/<mẫu>/diagrams
```

Mọi mẫu dùng chung bộ vẽ `scripts/common/drawio_kit.py` (đen trắng, sửa được trong draw.io).

## Spec và ticket

[Spec](../.scratch/design-patterns-redesign/spec.md) · [Danh sách ticket](../.scratch/design-patterns-redesign/tickets.md) · GitHub issue #21
