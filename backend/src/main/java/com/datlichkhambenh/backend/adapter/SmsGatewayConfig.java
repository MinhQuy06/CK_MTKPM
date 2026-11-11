package com.datlichkhambenh.backend.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Chọn Adaptee cho {@link SmsChannelAdapter}: eSMS thật khi bật cờ, ngược lại là bản "đã tắt". */
@Configuration
public class SmsGatewayConfig {

    @Bean
    @ConditionalOnProperty(name = "app.sms.enabled", havingValue = "true")
    SmsGatewayClient esmsGatewayClient(ObjectMapper json,
                                       @Value("${app.sms.esms.endpoint}") String endpoint,
                                       @Value("${app.sms.esms.api-key:}") String apiKey,
                                       @Value("${app.sms.esms.secret-key:}") String secretKey,
                                       @Value("${app.sms.esms.sms-type:8}") String smsType,
                                       @Value("${app.sms.esms.sandbox:false}") boolean sandbox) {
        return new EsmsGatewayClient(json, endpoint, apiKey, secretKey, smsType, sandbox);
    }

    @Bean
    @ConditionalOnMissingBean(SmsGatewayClient.class)
    SmsGatewayClient disabledSmsGatewayClient() {
        return new DisabledSmsGatewayClient();
    }
}
