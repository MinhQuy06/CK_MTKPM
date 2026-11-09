# Giá đặt lịch theo Strategy

Luồng mới POST /api/v1/appointments/confirm yêu cầu strategyKey, expectedBasePrice, expectedDiscountRate, expectedPrice và scheduleId. Controller gắn quoteRequired nội bộ; JSON client không thể tắt cờ này. Thiếu thông tin bị từ chối. POST /api/v1/appointments cũ tiếp tục mặc định Tiêu chuẩn khi không gửi loại/báo giá.

Server khóa Schedule, kiểm tra ca/bác sĩ/người bệnh rồi dùng PricingContext tính lại từ phí bác sĩ. Giá client chỉ dùng đối chiếu: base, rate hoặc giá cuối khác thì trả 409 PRICE_CHANGED, chưa tạo lịch hoặc gửi email. SLOT_UNAVAILABLE phân biệt mất ca.

Lịch mới lưu pricingKey/basePrice/discountRate/price; lịch cũ giữ metadata null. price là phí khám đã chốt, không phải xác nhận đã thu tiền. Email dùng price đã lưu qua cơ chế AFTER_COMMIT hiện có; không gửi khi rollback.

Tiền HALF_UP đến đồng. Tỷ lệ dùng DECIMAL(19,18), tối đa 18 chữ số thập phân để không làm sai snapshot. Chạy upgrade SQL theo thứ tự 001–004 trước triển khai trên database hiện hữu; không backfill dữ liệu lịch sử không biết.

Kiểm tra nhanh: bash scripts/maven.sh -q -f backend/pom.xml test; npm --prefix frontend test. MySQL contract: đặt CK_MYSQL_URL trỏ tới database tổng hợp kiểm thử rồi chạy MySqlBookingPricingIntegrationTests. Script scripts/strategy/test-mysql-upgrades.py chỉ dùng container ck-mtkpm-strategy-mysql và schema tổng hợp ck_mtkpm_upgrade_test để kiểm tra giữ giá lịch sử qua nâng cấp.

Class + Sequence đặt lịch nằm trong docs/strategy/diagrams/consultation-booking-*.drawio, cùng PNG đen trắng xuất cục bộ. Hai sơ đồ này bổ sung cặp báo giá, tổng cộng đúng 4 draw.io và 4 PNG.

## Bảo hiểm/VIP — ticket #15

Cả ba Strategy được dùng khi đặt lịch. Ưu đãi yêu cầu quyền đã duyệt hợp lệ cả hôm nay/ngày khám; server khóa Schedule → Doctor → BenefitRequest và kiểm tra trạng thái/ngày dưới khóa. Đọc ứng viên bằng ID trước khi khóa tránh dùng entity cũ trong persistence context. Thu hồi dùng cùng khóa quyền; phí bác sĩ được giữ ổn định đến commit. BENEFIT_UNAVAILABLE trả 403, không tự chuyển loại. Snapshot thêm benefitRequestId, vẫn giữ nguyên sau khi quyền bị thu hồi. Chạy migration 005 sau 004. Các test race điều khiển một cập nhật đang giữ khóa rồi xác nhận booking phải đợi và xét dữ liệu sau commit, chạy cả H2/MySQL.

## Xác nhận lại giá — ticket #16

PRICE_CHANGED làm giao diện lấy lại báo giá cùng ca/loại, hiển thị phí gốc, tỷ lệ và tiền cũ → mới. Nút chuyển thành “Xác nhận giá mới và đặt lịch”; không tự gửi POST lần nữa. Nếu giá tiếp tục đổi, lặp lại bước xem/xác nhận, giữ triệu chứng. Trong khi POST đang xử lý, khóa các lựa chọn và đóng modal để tránh người dùng đổi ca nhưng nhận kết quả của ca trước. GET báo giá có phiên bản request để bỏ response cũ.

Strategy tách ba thuật toán khỏi luồng đặt lịch: PricingContext nhận registry theo interface, nên thêm thuật toán không cần sửa phép tính của Context (OCP), controller/service phụ thuộc abstraction (DIP), mỗi Strategy chỉ giữ chính sách tính phí (SRP). Eligibility là điều kiện sử dụng, không phải thuật toán thứ tư; không gộp Bảo hiểm và VIP. Với phí gốc 300.000đ: Tiêu chuẩn 300.000đ, Bảo hiểm 60.000đ, VIP 270.000đ.

Lợi ích: dùng chung phép tính giữa báo giá/đặt, kiểm thử từng chính sách, snapshot giải thích được giá lịch sử. Hạn chế: thêm lớp/điều phối; phải cấu hình tỷ lệ nhất quán giữa instance; xác minh quyền là quy trình nội bộ thủ công; khóa bác sĩ làm các booking cùng bác sĩ chờ nhau ngắn hạn. Không phải hệ thống thanh toán hoặc kết nối cơ quan bảo hiểm.

## Ngày/giờ ca trong xác nhận nghiêm ngặt

Quote trả workDate/startTime/endTime. POST /confirm bắt buộc expectedWorkDate/expectedStartTime/expectedEndTime ngoài các trường giá; kiểm tra dưới khóa Schedule. Ca bị xóa trả 404 SLOT_UNAVAILABLE, ca bị đổi giờ/ngày trả 409 SLOT_UNAVAILABLE; cả hai kích hoạt chọn lại ca, giữ triệu chứng. UI đối chiếu ca đang hiển thị ngay khi quote về, và dùng ngày/giờ lịch lưu trong thông báo thành công. POST legacy giữ tương thích.
