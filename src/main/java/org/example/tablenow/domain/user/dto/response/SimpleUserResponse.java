package org.example.tablenow.domain.user.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SimpleUserResponse {
    private final Long id;
    private final String message;
}
