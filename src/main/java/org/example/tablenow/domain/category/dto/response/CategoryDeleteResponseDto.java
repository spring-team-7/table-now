package org.example.tablenow.domain.category.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class CategoryDeleteResponseDto {
    private final Long categoryId;
    private final String message;

    @Builder
    public CategoryDeleteResponseDto(Long categoryId, String message) {
        this.categoryId = categoryId;
        this.message = message;
    }

    public static CategoryDeleteResponseDto fromCategory(Long id) {
        return CategoryDeleteResponseDto.builder()
                .categoryId(id)
                .message("삭제되었습니다.")
                .build();
    }
}
