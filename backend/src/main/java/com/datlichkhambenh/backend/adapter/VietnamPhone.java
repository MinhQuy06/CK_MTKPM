package com.datlichkhambenh.backend.adapter;

import java.util.Optional;
import java.util.regex.Pattern;

/** Đổi số điện thoại phòng khám lưu sang dạng quốc tế (+84) mà nhà cung cấp SMS yêu cầu. */
public final class VietnamPhone {
    private static final Pattern SEPARATORS = Pattern.compile("[\\s.\\-]");
    private static final Pattern SUBSCRIBER = Pattern.compile("[1-9]\\d{8}");

    private VietnamPhone() {
    }

    /** "0912 345 678", "84912345678", "+84912345678" → "+84912345678"; dạng khác → rỗng. */
    public static Optional<String> toInternational(String stored) {
        if (stored == null) {
            return Optional.empty();
        }
        String digits = SEPARATORS.matcher(stored).replaceAll("");
        String subscriber;
        if (digits.startsWith("+84")) {
            subscriber = digits.substring(3);
        } else if (digits.startsWith("84") && digits.length() == 11) {
            subscriber = digits.substring(2);
        } else if (digits.startsWith("0")) {
            subscriber = digits.substring(1);
        } else {
            return Optional.empty();
        }
        return SUBSCRIBER.matcher(subscriber).matches() ? Optional.of("+84" + subscriber) : Optional.empty();
    }
}
