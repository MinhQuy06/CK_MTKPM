# 04: Thu hồi và gia hạn quyền hưởng, giữ lịch sử

Status: ready-for-agent

**Parent:** https://github.com/MinhQuy06/CK_MTKPM/issues/1

**GitHub:** https://github.com/MinhQuy06/CK_MTKPM/issues/11

**What to build:** Admin thu hồi quyền đã cấp kèm lý do; bệnh nhân thấy quyền bị thu hồi và gửi yêu cầu mới để gia hạn. Các quyết định cũ vẫn đọc lại được.

Thực hiện trong repository MinhQuy06/CK_MTKPM, branch thaihoa, tại checkout người dùng đã chỉ định. Tuân theo spec cha; sau mỗi ticket hoàn tất phải commit và push lên thaihoa theo chỉ dẫn mới nhất; không mở PR, merge hay deploy. Kiểm thử chính qua API, bổ sung kiểm thử giao diện cho hành vi của ticket. Đồng bộ frontend riêng và bản static backend phục vụ khi thay đổi giao diện.

**Blocked by:** 03: Admin duyệt hoặc từ chối yêu cầu quyền hưởng

- [ ] Admin thu hồi quyền đang được cấp qua giao diện/API, ghi lý do, người và thời điểm thu hồi; người không có quyền Admin không thực hiện được.
- [ ] Bệnh nhân xem được tình trạng bị thu hồi/hết hiệu lực và lịch sử quyết định phù hợp; không được sửa hoặc xóa lịch sử.
- [ ] Gia hạn bằng yêu cầu mới theo luồng duyệt đã có, không ghi đè thời hạn hoặc quyết định của hồ sơ cũ.
- [ ] Quyền bị thu hồi không còn đủ điều kiện cho báo giá/đặt mới; hết hạn được xác định nhất quán từ thời hạn theo múi giờ Việt Nam.
- [ ] Lưu đủ trạng thái và lịch sử để kiểm tra thay đổi đồng thời và để ticket đặt lịch truy nguyên quyền được dùng.
- [ ] Kiểm thử API/giao diện thu hồi, phân quyền, lịch sử giữ nguyên, gửi yêu cầu gia hạn và chỉ được hưởng sau khi yêu cầu mới được duyệt.
- [ ] Không tạo UML riêng; không sửa giá của lịch đã xác nhận khi trạng thái quyền hưởng thay đổi.
