package org.example.tablenow.domain.user.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.image.annotation.ImageUrlPattern;
import org.example.tablenow.global.util.RegexConstants;

@Builder
@Getter
public class UpdateProfileRequest {

    private String nickname;

    @Pattern(
            regexp = RegexConstants.PHONE_NUMBER_REGEX,
            message = "핸드폰 번호 형식이 올바르지 않습니다. 숫자만 입력해주세요."
    )
    private String phoneNumber;

    @ImageUrlPattern
    private String imageUrl;
}
