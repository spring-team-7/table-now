package org.example.tablenow.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserDeleteRequest {

    @NotBlank(message = "비밀번호 입력은 필수입니다.")
    private String password;
}
