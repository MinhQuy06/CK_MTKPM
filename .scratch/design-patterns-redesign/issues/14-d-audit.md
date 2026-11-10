# 14: Decorator: ghi lịch sử thông báo (bảng notification_log)

**What to build:** Mỗi lần gửi email được ghi một dòng lịch sử (loại, mã lịch hẹn, kênh, kết quả, ghi chú, thời điểm).

**Blocked by:** 13

**Status:** done

**GitHub:** #35

- [x] Bảng `notification_log` do Hibernate tạo (`schema.sql` của repo cố ý không chứa DDL) + file nâng cấp MySQL `db/upgrade/decorator/001-notification-log.sql`
- [x] Test qua API: đặt lịch → 1 dòng EMAIL/SENT đúng loại và mã
