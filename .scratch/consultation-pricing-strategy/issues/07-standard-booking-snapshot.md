# 07: Đặt lịch Tiêu chuẩn và lưu snapshot giá

Status: completed

**Parent:** https://github.com/MinhQuy06/CK_MTKPM/issues/1

**GitHub:** https://github.com/MinhQuy06/CK_MTKPM/issues/14

**What to build:** Bệnh nhân xác nhận báo giá Tiêu chuẩn, server tính lại và lưu giá đã chốt. Chi tiết/lịch sử và email phản ánh snapshot, kể cả sau khi phí bác sĩ hoặc cấu hình thay đổi.

Thực hiện trong repository MinhQuy06/CK_MTKPM, branch thaihoa, tại checkout người dùng đã chỉ định. Tuân theo spec cha; sau mỗi ticket hoàn tất phải commit và push lên thaihoa theo chỉ dẫn mới nhất; không mở PR, merge hay deploy. Kiểm thử chính qua API, bổ sung kiểm thử giao diện cho hành vi của ticket. Đồng bộ frontend riêng và bản static backend phục vụ khi thay đổi giao diện.

**Blocked by:** 01: Báo giá Tiêu chuẩn qua Strategy

- [x] Giao diện xác nhận và API dùng Strategy Tiêu chuẩn tại server với phí bác sĩ hiện tại; không dùng số tiền client gửi làm giá lưu.
- [x] Client mới gửi thông tin báo giá để đối chiếu. Giá thay đổi bị từ chối bằng lỗi riêng trước khi tạo lịch; giao diện thông báo và cho xem lại giá. Luồng giữ triệu chứng/xác nhận lại hoàn chỉnh thuộc ticket tiếp theo.
- [x] Lưu snapshot giá gốc, loại Strategy, tỷ lệ và giá cuối; chi tiết/lịch sử hiển thị đúng, email lấy giá đã lưu. Không tính lại giá lịch cũ khi đổi phí/cấu hình.
- [x] Thay đổi schema mang tính bổ sung, có nâng cấp database hiện hữu và kiểm tra dữ liệu cũ; không chế metadata quyền/loại giá của các lịch lịch sử.
- [x] Client cũ chưa gửi loại vẫn đặt Tiêu chuẩn theo hợp đồng tương thích được ghi rõ; loại rỗng/không hỗ trợ bị từ chối. Không cho bỏ thông tin xác nhận trong luồng mới để vượt kiểm tra.
- [x] Giữ JWT, ownership, khóa Schedule và giao dịch. Hai người đặt cùng ca chỉ một lịch thành công; kiểm tra khóa/tranh chấp trên MySQL test, không chỉ H2.
- [x] Giữ listener AFTER_COMMIT và hàng đợi đang có: rollback không phát xác nhận, commit hợp lệ phát nội dung theo snapshot. Kiểm thử phải đi qua commit thật khi kiểm tra AFTER_COMMIT.
- [x] Kiểm thử API tiền giả mạo, đổi giá, snapshot, tương thích client cũ, nâng cấp dữ liệu, tranh chấp và thông báo.
- [x] Bàn giao cặp Class + Sequence đặt lịch bằng draw.io chỉnh sửa được và PNG đen trắng; thuyết minh vai trò Strategy và lưu snapshot. Không vẽ UML quản lý quyền hưởng.

Verification: 95 Maven tests (H2 + real MySQL, 0 skipped) + 10 Node tests passed; 4 MySQL upgrade scripts preserve old data; Chrome quote→strict confirm→snapshot readback passed; booking UML/PNGs exported offline and inspected.
