# 15: Decorator: chống gửi trùng

**What to build:** Thông báo cùng loại cho cùng lịch hẹn đã gửi thành công thì không gửi lại, chỉ ghi SKIPPED_DUPLICATE.

**Blocked by:** 14

**Status:** ready-for-agent

**GitHub:** #36

- [ ] Test qua bean: nhắc lịch 2 lần → 1 bản tin, 2 dòng log (SENT, SKIPPED_DUPLICATE)
