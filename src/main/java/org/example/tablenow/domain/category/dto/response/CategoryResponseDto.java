package org.example.tablenow.domain.category.dto.response;

import lombok.Getter;
import org.example.tablenow.domain.category.entity.Category;

@Getter
public class CategoryResponseDto {
    private final Long categoryId;
    private final String name;

    public CategoryResponseDto(Long categoryId, String name) {
        this.categoryId = categoryId;
        this.name = name;
    }

    public static CategoryResponseDto fromCategory(Category category) {
        return new CategoryResponseDto(category.getId(), category.getName());
    }
}
