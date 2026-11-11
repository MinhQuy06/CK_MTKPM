package com.datlichkhambenh.backend.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Adaptee giả lập chỉ dùng trong test (không nằm trong bản chạy thật).
 * Không gửi gì thật; mọi tin được nhận chỉ lưu trong bộ nhớ và ghi log.
 */
@Slf4j
@Component
public class SimulatedSmsGatewayClient implements SmsGatewayClient {

    public record SubmittedSms(String msisdn, String text) {
    }

    private final List<SubmittedSms> sent = new CopyOnWriteArrayList<>();

    @Override
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
