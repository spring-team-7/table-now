package org.example.tablenow.domain.user.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import org.example.tablenow.global.util.RegexConstants;

@Getter
public class UpdateProfileRequest {

    private String nickname;

    @Pattern(
            regexp = RegexConstants.PHONE_NUMBER_REGEX,
            message = "핸드폰 번호 형식이 올바르지 않습니다. 숫자만 입력해주세요."
    )
    private String phoneNumber;
}
