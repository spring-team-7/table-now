package org.example.tablenow.domain.category.dto.response;

import lombok.Getter;

@Getter
public class CategoryDeleteResponseDto {
    private final Long id;
    private final String message;

    public CategoryDeleteResponseDto(Long id, String message) {
        this.id = id;
        this.message = message;
    }

    public static CategoryDeleteResponseDto fromCategory(Long id) {
        return new CategoryDeleteResponseDto(id, "삭제되었습니다.");
    }
}
