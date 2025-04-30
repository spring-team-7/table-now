package org.example.tablenow.domain.category.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.category.dto.request.CategoryRequestDto;
import org.example.tablenow.domain.category.dto.response.CategoryDeleteResponseDto;
import org.example.tablenow.domain.category.dto.response.CategoryResponseDto;
import org.example.tablenow.domain.category.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // 카테고리 등록
    @Secured("ROLE_ADMIN")
    @PostMapping("/v1/admin/categories")
    public ResponseEntity<CategoryResponseDto> createCategory(@Valid @RequestBody CategoryRequestDto requestDto) {
        return ResponseEntity.ok(categoryService.createCategory(requestDto));
    }

    // 카테고리 수정
    @Secured("ROLE_ADMIN")
    @PatchMapping("/v1/admin/categories/{categoryId}")
    public ResponseEntity<CategoryResponseDto> updateCategory(@PathVariable Long categoryId, @Valid @RequestBody CategoryRequestDto requestDto) {
        return ResponseEntity.ok(categoryService.updateCategory(categoryId, requestDto));
    }

    // 카테고리 삭제
    @Secured("ROLE_ADMIN")
    @DeleteMapping("/v1/admin/categories/{categoryId}")
    public ResponseEntity<CategoryDeleteResponseDto> deleteCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(categoryService.deleteCategory(categoryId));
    }

    // 카테고리 목록 조회
    @GetMapping("/v1/categories")
    public ResponseEntity<List<CategoryResponseDto>> getCategories() {
        return ResponseEntity.ok(categoryService.findAllCategories());
    }
}
