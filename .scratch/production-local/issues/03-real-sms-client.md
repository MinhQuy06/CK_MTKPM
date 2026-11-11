# 03 — Client SMS thật (eSMS)

Status: resolved

Blocked by: 01

Thực hiện mục 4 của [spec](../spec.md).

- Client HTTP eSMS thay `SimulatedSmsGatewayClient` ở `src/main`; khoá API trong env.
- `SMS_ENABLED` mặc định tắt: tắt thì kênh SMS bỏ qua và ghi log rõ, không lỗi.
- Chuyển bản giả lập vào `src/test`; `SmsChannelAdapterTests` và test Decorator/SMS vẫn xanh.
- Xong khi: `grep -rni simulat backend/src/main` rỗng; thử 1 tin thật khi bật cờ (tuỳ chọn, mất phí).

## Comments

Hoàn tất trên nhánh `feat/production-local`.
- Adaptee giờ là interface `SmsGatewayClient` (DIP). `SmsChannelAdapter` giữ nguyên vai trò Adapter, chỉ phụ thuộc interface.
- `EsmsGatewayClient` (thật, `java.net.http`): POST JSON tới eSMS, `CodeResult=100` → OK, mã khác → REJECTED, lỗi mạng/HTTP → ném ngoại lệ để `SmsNotificationDecorator` ghi FAILED mà không phá email/đặt lịch. Thiếu `ESMS_API_KEY`/`ESMS_SECRET_KEY` khi bật thì app không khởi động.
- `SMS_ENABLED=false` (mặc định): `DisabledSmsGatewayClient`, kết quả `SKIPPED_DISABLED` ghi vào lịch sử thông báo, không báo lỗi. `ESMS_SANDBOX=true` để eSMS nhận mà không gửi/không trừ tiền.
- `SimulatedSmsGatewayClient` chuyển sang `src/test`.
- Test mới: `EsmsGatewayClientTests` (máy chủ HTTP cục bộ thay esms.vn), `DisabledSmsChannelTests`. `mvn test`: 196 chạy, 0 lỗi, 17 bỏ qua.
- **Chưa kiểm chứng với eSMS thật**: tên trường và `SmsType=8` viết theo hiểu biết về API eSMS, cần đối chiếu tài liệu eSMS và thử một tin ở chế độ sandbox trước khi tin vào.
- Còn sót: cờ `simulated` (luôn `false`) trong `ConsultationQuoteDto` và các lớp Strategy là di sản của giai đoạn trước, nằm trong hợp đồng API và test Strategy, chưa gỡ. `grep` "simulat" trong `src/main` chỉ còn các chỗ này.
- Issue 05: thêm `SMS_ENABLED`, `ESMS_*` vào `.env.example` và compose.
