package org.example.tablenow.domain.user.dto.request;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserDeleteRequest {

    private String password;
}
