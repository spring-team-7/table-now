package org.example.tablenow.domain.user.dto.request;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UpdatePasswordRequest {

    private String password;
    private String newPassword;
}
