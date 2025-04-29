package org.example.tablenow.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class AccessTokenResponse {

    private final String accessToken;

    public static AccessTokenResponse fromTokenResponse(TokenResponse response) {
        return AccessTokenResponse.builder()
                .accessToken(response.getAccessToken())
                .build();
    }
}
