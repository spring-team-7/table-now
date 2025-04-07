package org.example.tablenow.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
public class AccessTokenResponse {

    private final String accessToken;

    public static AccessTokenResponse fromTokenResponse(TokenResponse response) {
        return AccessTokenResponse.builder()
                .accessToken(response.getAccessToken())
                .build();
    }
}
