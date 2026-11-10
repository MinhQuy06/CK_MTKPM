package com.datlichkhambenh.backend.adapter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Adaptee (GoF Adapter): stands in for a third-party SMS vendor SDK. Its interface is the vendor's,
 * not ours: an international MSISDN, plain text, and a numeric status code instead of exceptions.
 * Simulated: nothing is sent; every accepted message is kept in memory and logged.
 */
@Slf4j
@Component
public class SimulatedSmsGatewayClient {
    public static final int OK = 0;
    public static final int REJECTED = 400;

    public record SubmittedSms(String msisdn, String text) {
    }

    private final List<SubmittedSms> sent = new CopyOnWriteArrayList<>();

    /** Vendor call: returns {@link #OK} when accepted, another code when rejected. */
    public int submitSms(String msisdn, String text) {
        if (msisdn == null || !msisdn.startsWith("+")) {
            return REJECTED;
        }
        sent.add(new SubmittedSms(msisdn, text));
        log.info("[SMS giả lập] → {}: {}", msisdn, text);
        return OK;
    }

    /** Messages accepted so far (demo and tests). */
    public List<SubmittedSms> sent() {
        return sent;
    }
}
