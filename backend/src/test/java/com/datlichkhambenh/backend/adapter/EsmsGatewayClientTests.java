package com.datlichkhambenh.backend.adapter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Client eSMS thật nói chuyện với một máy chủ HTTP cục bộ thay cho esms.vn (không tốn tiền, không cần mạng). */
class EsmsGatewayClientTests {
    private final ObjectMapper json = new ObjectMapper();
    private HttpServer server;
    private final AtomicReference<JsonNode> received = new AtomicReference<>();
    private volatile int status = 200;
    private volatile String reply = "{\"CodeResult\":\"100\",\"SMSID\":\"abc\"}";

    @BeforeEach
    void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/send", exchange -> {
            received.set(json.readTree(exchange.getRequestBody().readAllBytes()));
            byte[] bytes = reply.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stop() {
        server.stop(0);
    }

    private EsmsGatewayClient client(boolean sandbox) {
        return new EsmsGatewayClient(json, "http://127.0.0.1:" + server.getAddress().getPort() + "/send",
                "KEY", "SECRET", "8", sandbox);
    }

    @Test
    void sendsVendorFormatAndReportsOkOnCode100() {
        int code = client(true).submitSms("+84912345678", "Da dat lich kham BK-1");

        assertEquals(SmsGatewayClient.OK, code);
        JsonNode body = received.get();
        assertEquals("84912345678", body.path("Phone").asText());
        assertEquals("Da dat lich kham BK-1", body.path("Content").asText());
        assertEquals("KEY", body.path("ApiKey").asText());
        assertEquals("SECRET", body.path("SecretKey").asText());
        assertEquals("1", body.path("Sandbox").asText());
    }

    @Test
    void vendorRejectionBecomesRejectedCode() {
        reply = "{\"CodeResult\":\"104\",\"ErrorMessage\":\"Sai thong tin\"}";

        assertEquals(SmsGatewayClient.REJECTED, client(false).submitSms("+84912345678", "x"));
    }

    @Test
    void httpFailureIsThrownSoTheDecoratorCanLogIt() {
        status = 500;

        assertThrows(IllegalStateException.class, () -> client(false).submitSms("+84912345678", "x"));
    }

    @Test
    void refusesToStartWithoutCredentials() {
        assertThrows(IllegalStateException.class,
                () -> new EsmsGatewayClient(json, "http://127.0.0.1:1/send", "", "", "8", false));
    }
}
