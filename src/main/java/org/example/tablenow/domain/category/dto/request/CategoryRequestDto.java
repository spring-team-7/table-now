package org.example.tablenow.domain.category.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class CategoryRequestDto {
    @NotBlank(message = "카테고리명은 필수값입니다.")
    private String name;
}
