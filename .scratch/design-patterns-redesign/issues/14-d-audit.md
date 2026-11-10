# 14: Decorator: ghi lịch sử thông báo (bảng notification_log)

**What to build:** Mỗi lần gửi email được ghi một dòng lịch sử (loại, mã lịch hẹn, kênh, kết quả, ghi chú, thời điểm).

**Blocked by:** 13

**Status:** ready-for-agent

**GitHub:** #35

- [ ] Bảng `notification_log` có trong `schema.sql` và tạo được ở profile test/dev
- [ ] Test qua API: đặt lịch → 1 dòng EMAIL/SENT đúng loại và mã
