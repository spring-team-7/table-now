package org.example.tablenow.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class UpdatePasswordRequest {

    private String password;
    private String newPassword;
}
