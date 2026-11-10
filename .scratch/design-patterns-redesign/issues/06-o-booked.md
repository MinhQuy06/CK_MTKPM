# 06: Observer: sự kiện đã đặt lịch + observer email

**What to build:** Đặt lịch phát sự kiện "đã đặt lịch"; observer email gửi xác nhận. Dịch vụ đặt lịch không còn gọi email trực tiếp.

**Blocked by:** None (can start immediately)

**Status:** done

**GitHub:** #27

- [x] Test qua API: đặt lịch → đúng 1 bản tin xác nhận, sau commit
- [x] Dịch vụ đặt lịch không còn phụ thuộc dịch vụ email ở luồng đặt lịch
- [x] Toàn bộ test Java, Node và trình duyệt hiện có vẫn pass, không sửa test cũ
