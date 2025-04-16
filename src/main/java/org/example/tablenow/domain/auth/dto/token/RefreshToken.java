package org.example.tablenow.domain.auth.dto.token;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RefreshToken {

    private String token;
    private Long userId;
}