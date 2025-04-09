package org.example.tablenow.domain.auth.oAuth.kakao;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoUserInfoResponse {

    private Long id;
    private KakaoAccount kakao_account;

    @Getter
    @NoArgsConstructor
    public static class KakaoAccount {
        private String email;
        private String name;
        private String phone_number;
        private Profile profile;

        @Getter
        @NoArgsConstructor
        public static class Profile {
            private String nickname;
            private String profile_image_url;
        }
    }
}
