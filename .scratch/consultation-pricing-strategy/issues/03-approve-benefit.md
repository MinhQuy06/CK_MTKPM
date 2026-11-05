# 03: Admin duyệt hoặc từ chối yêu cầu quyền hưởng

Status: ready-for-agent

**Parent:** https://github.com/MinhQuy06/CK_MTKPM/issues/1

**GitHub:** https://github.com/MinhQuy06/CK_MTKPM/issues/10

**What to build:** Admin xem yêu cầu bệnh nhân, đối chiếu hồ sơ ngoài hệ thống và duyệt hoặc từ chối có lý do. Bệnh nhân nhìn thấy kết quả; chỉ quyết định đã duyệt mới tạo quyền hưởng theo thời hạn xác nhận.

Thực hiện trong repository MinhQuy06/CK_MTKPM, branch thaihoa, tại checkout người dùng đã chỉ định. Tuân theo spec cha; sau mỗi ticket hoàn tất phải commit và push lên thaihoa theo chỉ dẫn mới nhất; không mở PR, merge hay deploy. Kiểm thử chính qua API, bổ sung kiểm thử giao diện cho hành vi của ticket. Đồng bộ frontend riêng và bản static backend phục vụ khi thay đổi giao diện.

**Blocked by:** 02: Bệnh nhân gửi yêu cầu quyền hưởng Bảo hiểm hoặc VIP

- [ ] Admin xem danh sách/chi tiết yêu cầu chờ; duyệt hoặc từ chối trong giao diện và API. Bệnh nhân/Doctor không có quyền quyết định.
- [ ] Khi duyệt, Admin xác nhận thời hạn hiệu lực; khi quyết định phải lưu kết quả, lý do, người/thời điểm quyết định và mã hồ sơ liên quan.
- [ ] Danh tính người duyệt lấy từ principal; không tin dữ liệu client khai. Không tiết lộ hồ sơ của bệnh nhân khác qua API bệnh nhân.
- [ ] Yêu cầu chờ hoặc bị từ chối không tạo quyền hưởng. Bệnh nhân xem được trạng thái kết quả và lý do phù hợp.
- [ ] Thời hạn dùng ngày Việt Nam, gồm cả ngày đầu và ngày cuối; dữ liệu quyền đã cấp đủ để kiểm tra cả ngày đặt và ngày khám sau này.
- [ ] Chuyển trạng thái nguyên tử: hai Admin không thể ghi hai quyết định mâu thuẫn hoặc cấp quyền hai lần cho một yêu cầu.
- [ ] Kiểm thử API/giao diện cho duyệt, từ chối, phân quyền, lý do/lịch sử, thời hạn không hợp lệ và quyết định đồng thời.
- [ ] Cập nhật schema/nâng cấp dữ liệu cần thiết; không tạo UML riêng cho quy trình Admin.
