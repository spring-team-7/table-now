package org.example.tablenow.domain.auth.oAuth;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Builder
@Getter
public class KakaoUserInfo {
    private String id;
    private String name;
    private String nickname;
    private String email;
    private String profileImage;
    private String phoneNumber;

    public static KakaoUserInfo from(Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");

        String id = String.valueOf(attributes.get("id"));
        String name = (String) kakaoAccount.get("name");
        String nickname = (String) profile.get("nickname");
        String email = (String) kakaoAccount.get("email");
        String profileImage = (String) profile.get("profile_image_url");
        String phoneNumber = (String) kakaoAccount.get("phone_number");

        return KakaoUserInfo.builder()
                .id(id)
                .name(name)
                .nickname(nickname)
                .email(email)
                .profileImage(profileImage)
                .phoneNumber(phoneNumber)
                .build();
    }
}

