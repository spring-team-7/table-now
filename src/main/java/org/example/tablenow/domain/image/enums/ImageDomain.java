package org.example.tablenow.domain.image.enums;

import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;

import java.util.Arrays;

public enum ImageDomain {
    USER, STORE;

    public static ImageDomain of(String domain) {
        return Arrays.stream(values())
                .filter(d -> d.name().equalsIgnoreCase(domain))
                .findFirst()
                .orElseThrow(() -> new HandledException(ErrorCode.INVALID_IMAGE_DOMAIN, "잘못된 이미지 도메인입니다: " + domain));
    }
}
