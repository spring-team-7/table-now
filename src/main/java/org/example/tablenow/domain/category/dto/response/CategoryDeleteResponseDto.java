package org.example.tablenow.domain.category.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CategoryDeleteResponseDto {
    private final Long categoryId;
    private final String message;

    public static CategoryDeleteResponseDto fromCategory(Long id) {
        return new CategoryDeleteResponseDto(id, "삭제되었습니다.");
    }
}
