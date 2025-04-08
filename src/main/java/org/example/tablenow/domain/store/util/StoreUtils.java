package org.example.tablenow.domain.store.util;

import org.springframework.util.StringUtils;

public class StoreUtils {

    public static String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return "";
        }
        return keyword.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("[^\\p{L}\\p{N} ]", "")   // 한글, 영문, 숫자 외 제거 (공백 허용)
                .toLowerCase();
    }
}
