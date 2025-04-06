package org.example.tablenow.domain.category.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.category.dto.request.CategoryRequestDto;
import org.example.tablenow.domain.category.dto.response.CategoryDeleteResponseDto;
import org.example.tablenow.domain.category.dto.response.CategoryResponseDto;
import org.example.tablenow.domain.category.entity.Category;
import org.example.tablenow.domain.category.repository.CategoryRepository;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategoryResponseDto saveCategory(@Valid CategoryRequestDto requestDto) {
        Category category = Category.builder().name(requestDto.getName()).build();
        Category savedCategory = categoryRepository.save(category);
        return CategoryResponseDto.fromCategory(savedCategory);
    }

    @Transactional
    public CategoryResponseDto updateCategory(Long id, @Valid CategoryRequestDto requestDto) {
        Category category = findCategory(id);
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
        List<Category> categories = categoryRepository.findAll();
        return categories.stream().map(CategoryResponseDto::fromCategory).collect(Collectors.toList());
    }

    private Category findCategory(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND.getDefaultMessage()));
    }
}
