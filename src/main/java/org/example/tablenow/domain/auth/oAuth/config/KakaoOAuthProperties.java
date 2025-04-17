package org.example.tablenow.domain.auth.oAuth.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "oauth.kakao")
public class KakaoOAuthProperties {
    private final String adminKey;
    private final String unlinkUri;
}