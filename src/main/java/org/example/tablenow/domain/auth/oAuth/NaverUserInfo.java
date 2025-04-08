package org.example.tablenow.domain.auth.oAuth;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class NaverUserInfo {
    private String id;
    private String name;
    private String nickname;
    private String email;
    private String profileImage;
    private String phoneNumber;

    public static NaverUserInfo from(Map<String, Object> attributes) {
        return NaverUserInfo.builder()
                .id((String) attributes.get("id"))
                .name((String) attributes.get("name"))
                .nickname((String) attributes.get("nickname"))
                .email((String) attributes.get("email"))
                .profileImage((String) attributes.get("profile_image"))
                .phoneNumber((String) attributes.get("mobile"))
                .build();
    }
}
