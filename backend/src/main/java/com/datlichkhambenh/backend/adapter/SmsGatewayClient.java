package com.datlichkhambenh.backend.adapter;

/**
 * Adaptee (GoF Adapter): giao diện của nhà cung cấp SMS bên thứ ba, không phải của phòng khám.
 * Số quốc tế (+84…), văn bản thô và mã trạng thái dạng số thay vì ngoại lệ.
 * Bản thật: {@link EsmsGatewayClient}; khi tắt SMS: {@link DisabledSmsGatewayClient}.
 */
public interface SmsGatewayClient {
    int OK = 0;
    int REJECTED = 400;
    /** SMS đang tắt theo cấu hình: không gửi và không phải lỗi. */
    int DISABLED = -1;

    /** Trả {@link #OK} khi nhà cung cấp nhận tin; mã khác khi từ chối. Lỗi mạng thì ném ngoại lệ. */
    int submitSms(String msisdn, String text);
}
