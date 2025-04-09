package org.example.tablenow.domain.auth.oAuth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NaverUserInfo {

    private String id;
    private String name;
    private String nickname;
    private String email;
    private String profileImage;
    private String phoneNumber;

    public static NaverUserInfo fromNaverUserInfoResponse(NaverUserInfoResponse.Response response) {
        return NaverUserInfo.builder()
                .id(response.getId())
                .name(response.getName())
                .nickname(response.getNickname())
                .email(response.getEmail())
                .profileImage(response.getProfile_image())
                .phoneNumber(response.getMobile())
                .build();
    }
}
