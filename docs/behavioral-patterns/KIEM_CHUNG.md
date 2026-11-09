# Hướng dẫn và kết quả kiểm chứng — Behavioral

## 1. Strategy định giá

Yêu cầu JDK 21. Từ thư mục gốc repository, chạy trong PowerShell:

```powershell
$pricingSources = Get-ChildItem -LiteralPath 'docs/behavioral-patterns/02-strategy/code' -Filter '*.java' | ForEach-Object FullName
$pricingBuild = Join-Path $env:TEMP ('behavioral-pricing-' + [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $pricingBuild | Out-Null
javac -encoding UTF-8 -d $pricingBuild $pricingSources
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
java -cp $pricingBuild PricingDemo
if ($LASTEXITCODE -ne 0) { throw 'Demo failed' }
```

Demo dùng Java thuần, không cần Maven, Spring, database hoặc quyền lợi bảo hiểm thật. Không chuyển các record Doctor/Patient của demo vào backend; chúng chỉ là mô hình độc lập phục vụ trình diễn.

Kết quả đã chạy với JDK 21:

```text
STANDARD = 350000 VND
INSURANCE = 70000 VND
VIP = 297500 VND
PASS reject: unverified insurance
PASS reject: unverified VIP
PASS reject: unknown policy
PASS reject: negative base price
PASS: prices, eligibility, rounding, zero, invalid input, extension.
Rates are fictional exercise assumptions; no real insurance calculation.
```

| Trường hợp | Kết quả kiểm tra |
| --- | --- |
| Giá cơ sở 350000, STANDARD | 350000 |
| Giá cơ sở 350000, INSURANCE hợp lệ trong demo | 70000 |
| Giá cơ sở 350000, VIP hợp lệ trong demo | 297500 |
| Quyền lợi bảo hiểm/VIP không hợp lệ | Từ chối |
| Mã chính sách không hỗ trợ | Từ chối |
| Giá cơ sở âm | Từ chối |
| Giá cơ sở bằng 0 | Trả 0 |
| Làm tròn giá 350001 | INSURANCE = 70000; VIP = 297501 |
| Đăng ký thêm SUMMER hệ số giả lập 0.90 | 315000; không sửa hàm ủy quyền của context |

Các kiểm tra trong `PricingDemo` ném `AssertionError` nếu kết quả khác kỳ vọng. Đây là kiểm chứng hành vi định giá, không phải benchmark hoặc kiểm thử tích hợp backend.

## 2. Observer

Các file trong `01-observer/code` được chép nguyên văn từ backend. Không biên dịch riêng folder này vì còn phụ thuộc Spring, Lombok, entity và các thư viện của dự án. Phụ lục cung cấp code đầy đủ để đối chiếu với phần phân tích và sơ đồ.

Các trường hợp dưới đây là **kế hoạch kiểm thử tích hợp**, chưa được chạy với RabbitMQ/SMTP trong phạm vi công việc này:

| Tình huống | Kết quả cần quan sát |
| --- | --- |
| Transaction đặt lịch commit | Listener AFTER_COMMIT được kích hoạt; message có đúng mã lịch hẹn |
| Transaction rollback sau khi phát event | Listener AFTER_COMMIT không publish message cho event đó |
| Phát event ngoài transaction | Listener hiện không bật fallbackExecution nên không xử lý |
| RabbitMQ lỗi sau commit | Lịch hẹn đã lưu; publisher ghi log; chưa có outbox tự phục hồi |
| SMTP lỗi khi consumer gửi | Lỗi nằm ở consumer; không giả định đã có retry/DLQ |

Không dùng kết quả tính giá của demo Strategy để suy ra Observer gửi mail thành công. Hai phần có phạm vi kiểm chứng khác nhau.

## 3. Tính thống nhất tài liệu

Sơ đồ Observer dùng đúng tên `BookingServiceImpl`, `publishAfterCommit(...)`, `consume(...)` và `SimpleMailMessage`. Sơ đồ Strategy được ghi rõ đề xuất/demo. Mỗi mẫu có hai trang Draw.io, đồng thời có hai ảnh PNG để đưa vào báo cáo. Mã backend không bị sửa trong lần làm tài liệu này.
