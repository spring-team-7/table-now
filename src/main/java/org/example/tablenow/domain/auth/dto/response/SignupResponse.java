package org.example.tablenow.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignupResponse {
    private final Long id;
    private final String email;
    private final String name;
    private final String nickname;
    private final String phoneNumber;
    private final String userRole;
}
