package org.example.tablenow.domain.image.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import org.example.tablenow.domain.image.enums.FileType;
import org.example.tablenow.global.util.RegexConstants;

@Getter
public class PresignedUrlRequest {

    @NotBlank(message = "파일 이름은 필수입니다.")
    @Pattern(
            regexp = RegexConstants.IMAGE_FILE_NAME_REGEX,
            message = "이미지 파일(jpg, jpeg, png, webp)만 업로드할 수 있습니다."
    )
    private String fileName;

    @NotNull(message = "파일 타입은 필수입니다.")
    private FileType fileType;
}
