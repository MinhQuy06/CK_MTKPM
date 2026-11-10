# 16: Decorator: gửi thêm SMS qua Adapter

**What to build:** Sau email, bệnh nhân có số nhận thêm SMS qua kênh Adapter; không có số thì ghi SKIPPED_NO_PHONE.

**Blocked by:** 14, 11, 12

**Status:** done

**GitHub:** #37

- [x] Test qua API: có số → client giả lập nhận 1 tin, log SMS/SENT
- [x] Test qua API: không có số → không có SMS, log SMS/SKIPPED_NO_PHONE, email vẫn gửi
