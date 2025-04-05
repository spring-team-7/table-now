package org.example.tablenow.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.global.util.RegexConstants;

@Getter
public class SignupRequest {

    @Email
    @NotBlank(message = "이메일 입력은 필수입니다.")
    private String email;

    @NotBlank(message = "비밀번호 입력은 필수입니다.")
    @Pattern(
            regexp = RegexConstants.PASSWORD_REGEX,
            message = "비밀번호 형식이 올바르지 않습니다."
    )
    private String password;

    @NotBlank(message = "이름 입력은 필수입니다.")
    private String name;

    private String nickname;

    @NotBlank(message = "핸드폰 번호 입력은 필수입니다.")
    @Pattern(
            regexp = RegexConstants.PHONE_NUMBER_REGEX,
            message = "핸드폰 번호 형식이 올바르지 않습니다. 숫자만 입력해주세요."
    )
    private String phoneNumber;

    @NotNull(message = "유저 타입 정보는 필수입니다.")
    private UserRole userRole;

    @Builder
    private SignupRequest(String email, String password, String name, String nickname, String phoneNumber, UserRole userRole) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.userRole = userRole;
    }
}
