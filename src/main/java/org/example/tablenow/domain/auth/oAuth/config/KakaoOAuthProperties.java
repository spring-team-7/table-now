package org.example.tablenow.domain.auth.oAuth.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "oauth.kakao")
public class KakaoOAuthProperties {
    private String adminKey;
    private String unlinkUri;
}