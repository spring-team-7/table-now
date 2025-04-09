package org.example.tablenow.global.util;

public class PhoneNumberNormalizer {
    public static String normalize(String rawPhoneNumber) {
        if (rawPhoneNumber == null || rawPhoneNumber.isBlank()) {
            return null;
        }

        // 하이픈, 공백, 괄호 제거
        String cleaned = rawPhoneNumber.replaceAll("[\\s\\-()]", "");

        // +82 → 0 변환
        if (cleaned.startsWith("+82")) {
            cleaned = "0" + cleaned.substring(3);
        }

        // 숫자만 남기기
        return cleaned.replaceAll("[^0-9]", "");
    }
}
