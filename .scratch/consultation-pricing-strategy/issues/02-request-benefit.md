# 02: Bệnh nhân gửi yêu cầu quyền hưởng Bảo hiểm hoặc VIP

Status: ready-for-agent

**Parent:** https://github.com/MinhQuy06/CK_MTKPM/issues/1

**GitHub:** https://github.com/MinhQuy06/CK_MTKPM/issues/9

**What to build:** Bệnh nhân gửi yêu cầu xác minh với loại quyền, mã hồ sơ và thời hạn đề nghị; xem lại trạng thái chờ duyệt của chính mình. Yêu cầu chưa được duyệt chưa cấp quyền hưởng.

Thực hiện trong repository MinhQuy06/CK_MTKPM, branch thaihoa, tại checkout người dùng đã chỉ định. Tuân theo spec cha; sau mỗi ticket hoàn tất phải commit và push lên thaihoa theo chỉ dẫn mới nhất; không mở PR, merge hay deploy. Kiểm thử chính qua API, bổ sung kiểm thử giao diện cho hành vi của ticket. Đồng bộ frontend riêng và bản static backend phục vụ khi thay đổi giao diện.

**Blocked by:** None (can start immediately)

- [ ] Giao diện bệnh nhân có form gửi yêu cầu Bảo hiểm hoặc VIP và danh sách/chi tiết yêu cầu của mình với trạng thái chờ duyệt.
- [ ] API lấy bệnh nhân từ principal, không cho khai danh tính người khác, trạng thái đã duyệt, người duyệt hoặc quyền hưởng để tự cấp ưu đãi.
- [ ] Validate loại quyền, mã tham chiếu hồ sơ và khoảng ngày đề nghị; từ chối dữ liệu thiếu, sai định dạng hoặc ngày kết thúc trước ngày bắt đầu.
- [ ] Lưu yêu cầu và thời điểm gửi; không tải/lưu ảnh hồ sơ hoặc gọi hệ thống bên ngoài.
- [ ] Bổ sung dữ liệu và tài liệu schema theo cách tương thích, có cách nâng cấp database hiện hữu; giữ dữ liệu cũ.
- [ ] Kiểm thử API tạo/đọc yêu cầu, đọc chéo bệnh nhân, mạo danh, request sửa trạng thái và dữ liệu không hợp lệ; kiểm thử giao diện gửi và đọc trạng thái.
- [ ] Không thêm UML quản lý quyền hưởng: phần này hỗ trợ việc tính phí, không phải chức năng trực tiếp thực thi Strategy.
