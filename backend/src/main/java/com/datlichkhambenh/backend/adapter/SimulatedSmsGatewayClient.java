package com.datlichkhambenh.backend.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Adaptee (GoF Adapter): đóng vai SDK của nhà cung cấp SMS bên thứ ba. Giao diện là của nhà cung cấp, không phải
 * của phòng khám: số quốc tế (+84…), văn bản thô và mã trạng thái dạng số thay vì ngoại lệ.
 * Giả lập: không gửi gì thật; mọi tin được nhận chỉ lưu trong bộ nhớ và ghi log.
 */
@Slf4j
@Component
public class SimulatedSmsGatewayClient {
    public static final int OK = 0;
    public static final int REJECTED = 400;

    public record SubmittedSms(String msisdn, String text) {
    }

    private final List<SubmittedSms> sent = new CopyOnWriteArrayList<>();

    /** Lời gọi của nhà cung cấp: trả {@link #OK} khi nhận tin, mã khác khi từ chối. */
    public int submitSms(String msisdn, String text) {
        if (msisdn == null || !msisdn.startsWith("+")) {
            return REJECTED;
        }
        sent.add(new SubmittedSms(msisdn, text));
        log.info("[SMS giả lập] → {}: {}", msisdn, text);
        return OK;
    }

    /** Các tin đã nhận (dùng cho demo và test). */
    public List<SubmittedSms> sent() {
        return sent;
    }
}
