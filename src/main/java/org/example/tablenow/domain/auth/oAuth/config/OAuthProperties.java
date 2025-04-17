package org.example.tablenow.domain.auth.oAuth.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
public class OAuthProperties {

    private final Provider provider;
    private final Registration registration;

    @Getter
    @RequiredArgsConstructor
    public static class Provider {
        private final Kakao kakao;
        private final Naver naver;

        @Getter
        @RequiredArgsConstructor
        public static class Kakao {
            private final String authorizationUri;
            private final String tokenUri;
            private final String userInfoUri;
            private final String userNameAttribute;
        }

        @Getter
        @RequiredArgsConstructor
        public static class Naver {
            private final String authorizationUri;
            private final String tokenUri;
            private final String userInfoUri;
            private final String userNameAttribute;
        }
    }

    @Getter
    @RequiredArgsConstructor
    public static class Registration {
        private final Kakao kakao;
        private final Naver naver;

        @Getter
        @RequiredArgsConstructor
        public static class Kakao {
            private final String clientId;
            private final String clientSecret;
            private final String redirectUri;
            private final String authorizationGrantType;
        }

        @Getter
        @RequiredArgsConstructor
        public static class Naver {
            private final String clientId;
            private final String clientSecret;
            private final String redirectUri;
            private final String authorizationGrantType;
        }
    }
}
