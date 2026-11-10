# 08: Observer: sự kiện đã hủy lịch

**What to build:** Hủy lịch phát sự kiện "đã hủy lịch"; observer email gửi thông báo hủy, observer cache xóa cache slot.

**Blocked by:** 07

**Status:** ready-for-agent

**GitHub:** #29

- [ ] Test qua API: hủy → 1 bản tin hủy + 1 lần xóa cache
- [ ] Dịch vụ đặt lịch không còn phụ thuộc dịch vụ email và cache
- [ ] Toàn bộ test Java, Node và trình duyệt hiện có vẫn pass, không sửa test cũ
