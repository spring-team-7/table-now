package org.example.tablenow.domain.category.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.category.dto.request.CategoryRequestDto;
import org.example.tablenow.domain.category.dto.response.CategoryDeleteResponseDto;
import org.example.tablenow.domain.category.dto.response.CategoryResponseDto;
import org.example.tablenow.domain.category.entity.Category;
import org.example.tablenow.domain.category.repository.CategoryRepository;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponseDto saveCategory(CategoryRequestDto requestDto) {
        validateExistCategory(requestDto);
        Category category = Category.builder().name(requestDto.getName()).build();
        Category savedCategory = categoryRepository.save(category);
        return CategoryResponseDto.fromCategory(savedCategory);
    }

    @Transactional
    public CategoryResponseDto updateCategory(Long id, CategoryRequestDto requestDto) {
        Category category = findCategory(id);
        validateExistCategory(requestDto);
        category.updateName(requestDto.getName());
        return CategoryResponseDto.fromCategory(category);
    }

    @Transactional
    public CategoryDeleteResponseDto deleteCategory(Long id) {
        Category category = findCategory(id);
        categoryRepository.delete(category);
        return CategoryDeleteResponseDto.fromCategory(category.getId());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDto> findAllCategories() {
        List<Category> categories = categoryRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        return categories.stream().map(CategoryResponseDto::fromCategory).collect(Collectors.toList());
    }

    public Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new HandledException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    public Optional<Category> findCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    private void validateExistCategory(CategoryRequestDto requestDto) {
        findCategoryByName(requestDto.getName()).ifPresent(it -> {
            throw new HandledException(ErrorCode.CATEGORY_ALREADY_EXISTS);
        });
    }
}
