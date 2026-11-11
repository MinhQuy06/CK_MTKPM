package com.datlichkhambenh.backend.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Adaptee thật: gọi REST API của eSMS (esms.vn). CodeResult "100" nghĩa là nhà cung cấp đã nhận tin.
 * Lỗi mạng hoặc HTTP không phải 2xx thì ném ngoại lệ; {@code SmsNotificationDecorator} ghi lại là FAILED
 * mà không làm hỏng email hay việc đặt lịch.
 */
@Slf4j
public class EsmsGatewayClient implements SmsGatewayClient {
    static final String SUCCESS_CODE = "100";

    private final HttpClient http;
    private final ObjectMapper json;
    private final URI endpoint;
    private final String apiKey;
    private final String secretKey;
    private final String smsType;
    private final boolean sandbox;

    public EsmsGatewayClient(ObjectMapper json, String endpoint, String apiKey, String secretKey,
                             String smsType, boolean sandbox) {
        if (isBlank(apiKey) || isBlank(secretKey)) {
            throw new IllegalStateException("SMS_ENABLED=true nhưng thiếu ESMS_API_KEY hoặc ESMS_SECRET_KEY.");
        }
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        this.json = json;
        this.endpoint = URI.create(endpoint);
        this.apiKey = apiKey;
        this.secretKey = secretKey;
        this.smsType = smsType;
        this.sandbox = sandbox;
    }

    @Override
    public int submitSms(String msisdn, String text) {
        // eSMS nhận số dạng 84xxxxxxxxx, không có dấu '+'.
        String phone = msisdn.startsWith("+") ? msisdn.substring(1) : msisdn;
        Map<String, String> body = new LinkedHashMap<>();
        body.put("ApiKey", apiKey);
        body.put("SecretKey", secretKey);
        body.put("Phone", phone);
        body.put("Content", text);
        body.put("SmsType", smsType);
        body.put("IsUnicode", "0");
        body.put("Sandbox", sandbox ? "1" : "0");
        body.put("RequestId", UUID.randomUUID().toString());
        try {
            HttpRequest request = HttpRequest.newBuilder(endpoint)
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json.writeValueAsString(body)))
                    .build();
            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() / 100 != 2) {
                throw new IllegalStateException("eSMS trả HTTP " + response.statusCode());
            }
            JsonNode reply = json.readTree(response.body());
            String code = reply.path("CodeResult").asText("");
            if (SUCCESS_CODE.equals(code)) {
                log.info("[eSMS] Đã gửi tới {} (SMSID={})", phone, reply.path("SMSID").asText(""));
                return OK;
            }
            log.warn("[eSMS] Từ chối tin tới {}: CodeResult={} {}", phone, code, reply.path("ErrorMessage").asText(""));
            return REJECTED;
        } catch (IOException e) {
            throw new IllegalStateException("Không gọi được eSMS: " + e.getMessage(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Gọi eSMS bị ngắt", e);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
