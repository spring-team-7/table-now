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

    @Nested
    class 카테고리_등록 {
        @Test
        void 중복_카테고리_등록_시_예외_발생() {
            // given
            CategoryRequestDto dto = new CategoryRequestDto("한식");
            given(categoryRepository.existsByName(anyString())).willReturn(true);

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    categoryService.createCategory(dto)
            );
            assertEquals(exception.getMessage(), ErrorCode.CATEGORY_ALREADY_EXISTS.getDefaultMessage());
        }

        @Test
        void 등록_성공() {
            // given
            String categoryName = "한식";
            CategoryRequestDto dto = new CategoryRequestDto(categoryName);

            given(categoryRepository.existsByName(anyString())).willReturn(false);
            given(categoryRepository.save(any(Category.class))).willAnswer(invocation -> invocation.getArgument(0));

            // when
            CategoryResponseDto response = categoryService.createCategory(dto);

            // then
            assertNotNull(response);
            assertEquals(response.getName(), categoryName);
        }
    }

    @Nested
    class 카테고리_수정 {
        @Test
        void 존재하지_않는_카테고리_조회_시_예외_발생() {
            // given
            Long categoryId = 1L;
            CategoryRequestDto dto = new CategoryRequestDto();

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
            Long categoryId = 1L;
            Category category = Category.builder().id(categoryId).name("한식").build();
            CategoryRequestDto dto = new CategoryRequestDto();

            ReflectionTestUtils.setField(dto, "name", "한식");
            given(categoryRepository.findById(anyLong())).willReturn(Optional.of(category));
            given(categoryRepository.existsByName(anyString())).willReturn(true);

            // when & then
            HandledException exception = assertThrows(HandledException.class, () ->
                    categoryService.updateCategory(categoryId, dto)
            );
            assertEquals(exception.getMessage(), ErrorCode.CATEGORY_ALREADY_EXISTS.getDefaultMessage());
        }

        @Test
        void 수정_성공() {
            // given
            Long categoryId = 1L;
            Category category = Category.builder().id(categoryId).name("한식").build();
            CategoryRequestDto dto = new CategoryRequestDto();

            String requestName = "분식";
            ReflectionTestUtils.setField(dto, "name", requestName);
            given(categoryRepository.findById(anyLong())).willReturn(Optional.of(category));
            given(categoryRepository.existsByName(anyString())).willReturn(false);

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
            Long categoryId = 1L;
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
            Long categoryId = 1L;
            Category category = Category.builder().id(categoryId).name("한식").build();

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
        @Test
        void 조회_성공() {
            // given
            Category category1 = Category.builder().id(1L).name("한식").build();
            Category category2 = Category.builder().id(2L).name("중식").build();
            Category category3 = Category.builder().id(3L).name("일식").build();
            List<Category> categories = List.of(category1, category2, category3);

            given(categoryRepository.findAll(any(Sort.class))).willReturn(categories);

            // when
            List<CategoryResponseDto> response = categoryService.findAllCategories();

            // then
            assertNotNull(response);
            assertEquals(response.size(), categories.size());
            assertEquals(response.get(0).getName(), "한식");
        }
    }

}
