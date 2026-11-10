# 10: Adapter: kênh thông báo + client SMS giả lập + adapter

**What to build:** Hệ thống gửi thông báo qua một kênh chung; adapter SMS chuyển lời gọi sang client SMS giả lập có chữ ký khác (số quốc tế + nội dung).

**Blocked by:** None (can start immediately)

**Status:** done

**GitHub:** #31

- [x] Test qua bean kênh thông báo: client giả lập nhận đúng số và nội dung có mã lịch hẹn
- [x] Adapter dùng composition, không kế thừa client
