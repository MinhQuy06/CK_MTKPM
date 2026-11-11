# Báo giá khám Tiêu chuẩn — ticket #8

Bệnh nhân chọn ca trong dashboard và nhận báo giá từ server. API GET /api/v1/appointments/quote nhận scheduleId và strategyKey (mặc định STANDARD), yêu cầu JWT và PATIENT. Kết quả gồm giá gốc, tỷ lệ giảm, giá cuối và VND. Báo giá không giữ chỗ, tạo lịch hoặc gửi email.

ConsultationPricingStrategy là Strategy; StandardPricingStrategy là Concrete Strategy. PricingContext giữ registry bất biến, chọn thuật toán cho từng request và chuẩn hóa tiền. BookingOffer là client của Context, chịu trách nhiệm ca còn đặt được, quyền hưởng và lý do từ chối (trước 11/11/2026 là ConsultationQuoteService). Công thức Tiêu chuẩn mặc định: 300000 × (1 − 0) = 300000 VND.

Tỷ lệ giảm STANDARD_DISCOUNT_RATE là số trong [0,1]. BigDecimal chuẩn hóa phí gốc HALF_UP đến đồng, áp dụng tỷ lệ rồi làm tròn giá cuối; phí 0 hợp lệ, thiếu/âm hoặc tràn DECIMAL(12,2) bị từ chối. Việc cấu hình công thức không tạo quyền Bảo hiểm/VIP: các ticket sau bổ sung xét điều kiện riêng.

Giao diện bỏ kết quả cũ sau khi đổi ca/ngày/bác sĩ/loại hoặc đóng modal. Chỉ bật xác nhận khi báo giá hiện tại đã tải xong. Giai đoạn #8 giữ nguyên booking payload; ticket #14 tích hợp xác nhận giá và snapshot.

Class và Sequence cho báo giá ở docs/strategy/diagrams/consultation-quote-class.drawio và consultation-quote-sequence.drawio, cùng PNG đen trắng. Mở bằng draw.io để chỉnh sửa. Xuất PNG cục bộ bằng DRAWIO_BIN=/đường/dẫn/draw.io bash scripts/strategy/export-diagrams.sh; không tải XML lên dịch vụ ngoài.

Kiểm tra: bash scripts/maven.sh -q -f backend/pom.xml test; npm --prefix frontend test. Smoke trình duyệt dùng fixture H2 độc lập, xem hướng dẫn kiểm thử trong báo cáo chung sau khi hoàn thành chuỗi ticket.

## Bảo hiểm đã xác minh — ticket #12

InsurancePricingStrategy giảm mặc định 80%, tỷ lệ INSURANCE_DISCOUNT_RATE cấu hình trong [0,1]. Với phí 300000, giá phải trả là 60000 VND. BenefitService kiểm tra hồ sơ đã duyệt, chưa thu hồi và khoảng ngày bao gồm hôm nay lẫn ngày khám theo giờ Việt Nam. Danh tính lấy từ JWT. API pricing-options chỉ trả loại hiện đủ điều kiện; chọn bằng request sửa tay vẫn phải qua kiểm tra server. Không giới hạn demo/test và không cam kết khoản bảo hiểm hoàn trả. UML chỉ hiển thị BenefitService như cộng tác viên cần cho việc chọn Strategy.

## VIP đã xác minh — ticket #13

VipPricingStrategy giảm mặc định 10% (VIP_DISCOUNT_RATE), người bệnh trả 90%. Với phí 300000, giá VIP là 270000 VND. Registry nhận thêm Concrete Strategy mà không sửa thuật toán điều phối. Nếu có cả hai quyền, bệnh nhân chọn một loại; không cộng dồn Bảo hiểm/VIP. Mọi loại ưu đãi đều qua cùng kiểm tra người bệnh, trạng thái duyệt và ngày hiệu lực.
