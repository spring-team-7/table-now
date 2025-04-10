package org.example.tablenow.global.util;

public final class RegexConstants {

    // 소문자, 숫자 포함해서 8글자 이상의 비밀번호
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*\\d)[a-zA-Z0-9!@#$%^&*]{8,}$";

    // 10~11자리의 숫자 전화번호
    public static final String PHONE_NUMBER_REGEX = "^[0-9]{10,11}$";

    // 파일 이름에 확장자가 포함되어 있는지 검증 (.확장자 형식)
    public static final String IMAGE_FILE_NAME_REGEX = "^.+\\.(?i)(jpg|jpeg|png|webp)$";

    private RegexConstants() {
        // 인스턴스 생성 방지
    }
}
