package org.example.tablenow.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import org.example.tablenow.global.util.RegexConstants;

@Getter
public class UpdatePasswordRequest {

    @NotBlank(message = "비밀번호 입력은 필수입니다.")
    private String password;

    @NotBlank(message = "비밀번호 입력은 필수입니다.")
    @Pattern(
            regexp = RegexConstants.PASSWORD_REGEX,
            message = "비밀번호 형식이 올바르지 않습니다."
    )
    private String newPassword;
}
