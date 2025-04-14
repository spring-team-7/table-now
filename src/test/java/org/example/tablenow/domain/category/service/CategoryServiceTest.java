package org.example.tablenow.domain.category.service;

import org.example.tablenow.domain.category.dto.request.CategoryRequestDto;
import org.example.tablenow.domain.category.dto.response.CategoryDeleteResponseDto;
import org.example.tablenow.domain.category.dto.response.CategoryResponseDto;
import org.example.tablenow.domain.category.entity.Category;
import org.example.tablenow.domain.category.repository.CategoryRepository;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private CategoryService categoryService;

    Long categoryId = 1L;
    String categoryName = "한식";
    Category category = Category.builder().id(categoryId).name(categoryName).build();

    @Nested
    class 카테고리_등록 {
        CategoryRequestDto dto = new CategoryRequestDto();

        @BeforeEach
        void setUp() {
            ReflectionTestUtils.setField(dto, "name", categoryName);
        }

        @Test
        void 중복_카테고리_등록_시_예외_발생() {
            // given
            given(categoryRepository.findByName(anyString())).willReturn(Optional.of(category));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    categoryService.saveCategory(dto)
            );
            assertEquals(exception.getMessage(), ErrorCode.CATEGORY_ALREADY_EXISTS.getDefaultMessage());
        }

        @Test
        void 등록_성공() {
            // given
            given(categoryRepository.findByName(anyString())).willReturn(Optional.empty());
            given(categoryRepository.save(any(Category.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            CategoryResponseDto response = categoryService.saveCategory(dto);

            // then
            assertNotNull(response);
            assertEquals(response.getName(), categoryName);
        }
    }

    @Nested
    class 카테고리_수정 {
        CategoryRequestDto dto = new CategoryRequestDto();

        @Test
        void 존재하지_않는_카테고리_조회_시_예외_발생() {
            // given
            given(categoryRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    categoryService.updateCategory(categoryId, dto)
            );
            assertEquals(exception.getMessage(), ErrorCode.CATEGORY_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 중복_카테고리_등록_시_예외_발생() {
            // given
            ReflectionTestUtils.setField(dto, "name", categoryName);
            given(categoryRepository.findById(anyLong())).willReturn(Optional.of(category));
            given(categoryRepository.findByName(anyString())).willReturn(Optional.of(category));

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    categoryService.updateCategory(categoryId, dto)
            );
            assertEquals(exception.getMessage(), ErrorCode.CATEGORY_ALREADY_EXISTS.getDefaultMessage());
        }

        @Test
        void 수정_성공() {
            // given
            String requestName = "분식";
            ReflectionTestUtils.setField(dto, "name", requestName);
            given(categoryRepository.findById(anyLong())).willReturn(Optional.of(category));
            given(categoryRepository.findByName(anyString())).willReturn(Optional.empty());

            // when
            CategoryResponseDto response = categoryService.updateCategory(categoryId, dto);

            // then
            assertNotNull(response);
            assertEquals(response.getName(), requestName);
        }
    }

    @Nested
    class 카테고리_삭제 {
        @Test
        void 존재하지_않는_카테고리_조회_시_예외_발생() {
            // given
            given(categoryRepository.findById(anyLong())).willReturn(Optional.empty());

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    categoryService.deleteCategory(categoryId)
            );
            assertEquals(exception.getMessage(), ErrorCode.CATEGORY_NOT_FOUND.getDefaultMessage());
        }

        @Test
        void 삭제_성공() {
            // given
            given(categoryRepository.findById(anyLong())).willReturn(Optional.of(category));

            // when
            CategoryDeleteResponseDto response = categoryService.deleteCategory(categoryId);

            // then
            assertNotNull(response);
            assertEquals(response.getCategoryId(), categoryId);
        }
    }

    @Nested
    class 카테고리_목록_조회 {
        Category category1 = Category.builder().id(1L).name(categoryName).build();
        Category category2 = Category.builder().id(2L).name("중식").build();
        Category category3 = Category.builder().id(3L).name("일식").build();
        List<Category> categories = List.of(category1, category2, category3);

        @Test
        void 조회_성공() {
            // given
            given(categoryRepository.findAll(any(Sort.class))).willReturn(categories);

            // when
            List<CategoryResponseDto> response = categoryService.findAllCategories();

            // then
            assertNotNull(response);
            assertEquals(response.size(), categories.size());
            assertEquals(response.get(0).getName(), categoryName);
        }
    }

}
