package org.example.tablenow.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class SimpleUserResponse {
    private final Long id;
    private final String message;
}
