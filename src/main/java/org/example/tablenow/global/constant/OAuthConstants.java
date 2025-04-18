package org.example.tablenow.global.constant;

public final class OAuthConstants {

    // OAuth 응답 필드
    public static final String ACCESS_TOKEN = "access_token";

    // OAuth 요청 파라미터
    public static final String CLIENT_ID = "client_id";
    public static final String CLIENT_SECRET = "client_secret";
    public static final String REDIRECT_URI = "redirect_uri";
    public static final String GRANT_TYPE = "grant_type";
    public static final String CODE = "code";

    // Kakao 전용 요청 파라미터 및 인증 헤더
    public static final String KAKAO_ADMIN_AUTH_PREFIX = "KakaoAK ";
    public static final String TARGET_ID_TYPE = "target_id_type";
    public static final String TARGET_ID = "target_id";
    public static final String USER_ID = "user_id";

    private OAuthConstants() {
        // 인스턴스 생성 방지
    }
}