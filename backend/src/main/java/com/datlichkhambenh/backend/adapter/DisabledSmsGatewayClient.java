package com.datlichkhambenh.backend.adapter;

import lombok.extern.slf4j.Slf4j;

/** Dùng khi {@code app.sms.enabled=false}: không gửi gì và ghi log rõ ràng, để SMS tắt không bị nhầm với lỗi. */
@Slf4j
public class DisabledSmsGatewayClient implements SmsGatewayClient {
    @Override
    public int submitSms(String msisdn, String text) {
        log.info("[SMS tắt] Không gửi tới {} (đặt SMS_ENABLED=true để bật)", msisdn);
        return DISABLED;
    }
}
