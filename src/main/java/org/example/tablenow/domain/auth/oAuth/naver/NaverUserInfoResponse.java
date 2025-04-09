package org.example.tablenow.domain.auth.oAuth.naver;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NaverUserInfoResponse {

    private String resultcode;
    private String message;
    private Response response;

    @Getter
    @NoArgsConstructor
    public static class Response {
        private String id;
        private String name;
        private String nickname;
        private String email;
        private String profile_image;
        private String mobile;
    }
}
