package org.example.tablenow.domain.auth.oAuth;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class KakaoUserInfo {

    private String id;
    private String name;
    private String nickname;
    private String email;
    private String profileImage;
    private String phoneNumber;

    public static KakaoUserInfo fromKakaoUserInfoResponse(KakaoUserInfoResponse response) {
        KakaoUserInfoResponse.KakaoAccount account = response.getKakao_account();
        KakaoUserInfoResponse.KakaoAccount.Profile profile = account.getProfile();

        return KakaoUserInfo.builder()
                .id(String.valueOf(response.getId()))
                .name(account.getName())
                .nickname(profile.getNickname())
                .email(account.getEmail())
                .profileImage(profile.getProfile_image_url())
                .phoneNumber(account.getPhone_number())
                .build();
    }
}

