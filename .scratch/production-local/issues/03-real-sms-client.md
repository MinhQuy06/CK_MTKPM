# 03 — Client SMS thật (eSMS)

Status: ready-for-agent

Blocked by: 01

Thực hiện mục 4 của [spec](../spec.md).

- Client HTTP eSMS thay `SimulatedSmsGatewayClient` ở `src/main`; khoá API trong env.
- `SMS_ENABLED` mặc định tắt: tắt thì kênh SMS bỏ qua và ghi log rõ, không lỗi.
- Chuyển bản giả lập vào `src/test`; `SmsChannelAdapterTests` và test Decorator/SMS vẫn xanh.
- Xong khi: `grep -rni simulat backend/src/main` rỗng; thử 1 tin thật khi bật cờ (tuỳ chọn, mất phí).
