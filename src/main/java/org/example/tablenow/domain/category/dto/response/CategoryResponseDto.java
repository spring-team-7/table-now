package org.example.tablenow.domain.category.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.tablenow.domain.category.entity.Category;

@Getter
@AllArgsConstructor
public class CategoryResponseDto {
    private final Long categoryId;
    private final String name;

    public static CategoryResponseDto fromCategory(Category category) {
        return new CategoryResponseDto(category.getId(), category.getName());
    }
}
