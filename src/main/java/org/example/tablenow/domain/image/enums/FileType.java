package org.example.tablenow.domain.image.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;

import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum FileType {
    JPEG("image/jpeg"),
    PNG("image/png"),
    WEBP("image/webp");

    private final String mimeType;

    public static FileType of(String mimeType) {
        return Arrays.stream(values())
                .filter(f -> f.mimeType.equalsIgnoreCase(mimeType))
                .findFirst()
                .orElseThrow(() -> new HandledException(ErrorCode.INVALID_FILE_TYPE, "지원하지 않는 파일 형식입니다: " + mimeType));
    }
}