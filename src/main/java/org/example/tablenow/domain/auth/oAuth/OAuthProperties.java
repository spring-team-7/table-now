package org.example.tablenow.domain.auth.oAuth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "spring.security.oauth2.client")
public class OAuthProperties {

    private Provider provider;
    private Registration registration;

    @Getter
    @Setter
    public static class Provider {
        private Kakao kakao;
        private Naver naver;

        @Getter
        @Setter
        public static class Kakao {
            private String authorizationUri;
            private String tokenUri;
            private String userInfoUri;
            private String userNameAttribute;
        }

        @Getter
        @Setter
        public static class Naver {
            private String authorizationUri;
            private String tokenUri;
            private String userInfoUri;
            private String userNameAttribute;
        }
    }

    @Getter
    @Setter
    public static class Registration {
        private Kakao kakao;
        private Naver naver;

        @Getter
        @Setter
        public static class Kakao {
            private String clientId;
            private String clientSecret;
            private String redirectUri;
            private String authorizationGrantType;
        }

        @Getter
        @Setter
        public static class Naver {
            private String clientId;
            private String clientSecret;
            private String redirectUri;
            private String authorizationGrantType;
        }
    }
}
