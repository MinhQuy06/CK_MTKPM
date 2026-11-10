# 06: Observer: sự kiện đã đặt lịch + observer email

**What to build:** Đặt lịch phát sự kiện "đã đặt lịch"; observer email gửi xác nhận. Dịch vụ đặt lịch không còn gọi email trực tiếp.

**Blocked by:** None (can start immediately)

**Status:** ready-for-agent

**GitHub:** #27

- [ ] Test qua API: đặt lịch → đúng 1 bản tin xác nhận, sau commit
- [ ] Dịch vụ đặt lịch không còn phụ thuộc dịch vụ email ở luồng đặt lịch
- [ ] Toàn bộ test Java, Node và trình duyệt hiện có vẫn pass, không sửa test cũ
