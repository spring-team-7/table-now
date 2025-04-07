package org.example.tablenow.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class TokenResponse {

    private final String accessToken;

    private final String refreshToken;
}
