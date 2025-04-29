package org.example.tablenow.global.constant;

public final class SecurityConstants {

    public static final String BEARER_PREFIX = "Bearer ";
    public static final long REFRESH_TOKEN_TTL_SECONDS = 7 * 24 * 60 * 60L;

    private SecurityConstants() {
        // 인스턴스 생성 방지
    }
}