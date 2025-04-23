package org.example.tablenow.domain.store.service;

import org.example.tablenow.domain.category.entity.Category;
import org.example.tablenow.domain.store.dto.response.StoreDocumentResponseDto;
import org.example.tablenow.domain.store.entity.StoreDocument;
import org.example.tablenow.domain.store.enums.StoreSortField;
import org.example.tablenow.domain.store.repository.StoreElasticRepository;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class StoreSearchServiceTest {

    @Mock
    private StoreElasticRepository storeElasticRepository;
    @Mock
    private RedisTemplate<Object, Object> redisTemplate;
    @Mock
    private ValueOperations<Object, Object> valueOperations;
    @Mock
    private StoreService storeService;
    @InjectMocks
    private StoreSearchService storeSearchService;

    Long userId = 1L;
    Long ownerId = 2L;
    AuthUser authUser = new AuthUser(userId, "user@a.com", UserRole.ROLE_USER, "일반회원");
    User user = User.fromAuthUser(authUser);
    AuthUser authOwner = new AuthUser(ownerId, "owner@a.com", UserRole.ROLE_OWNER, "가게");
    User owner = User.fromAuthUser(authOwner);

    Long categoryId = 1L;
    Category category = Category.builder().id(categoryId).name("한식").build();

    Long storeId = 1L;
    StoreDocument storeDocument = StoreDocument.builder()
            .id(storeId)
            .name("맛있는 가게")
            .description("가게 설명입니다.")
            .address("서울특별시 강남구 테헤란로11길 1 1층")
            .imageUrl(null)
            .capacity(100)
            .startTime("09:00")
            .endTime("21:00")
            .deposit(10000)
            .rating(4.5)
            .ratingCount(100)
            .userId(userId)
            .userName(user.getName())
            .categoryId(categoryId)
            .categoryName(category.getName())
            .build();

    @Nested
    class 가게_검색_v3 {
        int page = 1;
        int size = 10;
        String sortField = "ratingCount";
        String sortOrder = "desc";
        Sort sortOption = Sort.by(Sort.Direction.fromString(sortOrder), StoreSortField.fromString(sortField));
        Pageable pageable = PageRequest.of(page - 1, size, sortOption);

        @Nested
        class 정렬_기준_예외 {
            @Test
            void asc_desc_외_정렬_입력_시_예외_발생() {
                // given
                String sortField = "name";
                String sortOrder = "average";

                // when & then
                HandledException exception = assertThrows(HandledException.class, () ->
                        storeSearchService.getStoresV3(authUser, 1, 10, sortField, sortOrder, null, null)
                );
                assertEquals(exception.getMessage(), ErrorCode.INVALID_ORDER_VALUE.getDefaultMessage());
            }

            @Test
            void 존재하지_않는_정렬_기준_입력_시_예외_발생() {
                // given
                String sortField = "description";
                String sortOrder = "asc";

                // when & then
                HandledException exception = assertThrows(HandledException.class, () ->
                        storeSearchService.getStoresV3(authUser, 1, 10, sortField, sortOrder, null, null)
                );
                assertEquals(exception.getMessage(), ErrorCode.INVALID_SORT_FIELD.getDefaultMessage());
            }
        }

        @Test
        void 가게_검색_cache_hit_시_Redis_조회() {
            // given
            given(redisTemplate.hasKey(anyString())).willReturn(true);
            Page<StoreDocumentResponseDto> result = new PageImpl<>(List.of(StoreDocumentResponseDto.fromStoreDocument(storeDocument)));
            given(redisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(anyString())).willReturn(result);

            // when
            Page<StoreDocumentResponseDto> response = storeSearchService.getStoresV3(authUser, 1, 10, sortField, sortOrder, null, null);

            // then
            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals(response.getContent().get(0).getStoreId(), storeId)
            );
        }

        @Test
        void 가게_검색_cache_miss_시_elastic_search_조회_결과_없음() {
            // given
            given(redisTemplate.hasKey(anyString())).willReturn(false);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);

            Page<StoreDocument> result = new PageImpl<>(Collections.emptyList());
            given(storeElasticRepository.searchByKeywordAndCategoryId(anyString(), anyLong(), any(Pageable.class))).willReturn(result);

            // when
            Page<StoreDocumentResponseDto> response = storeSearchService.getStoresV3(authUser, 1, 10, sortField, sortOrder, 1L, "test");

            // then
            assertNotNull(response);
            verify(redisTemplate.opsForValue(), never()).set(anyString(), any());
        }

        @Test
        void 가게_검색_cache_miss_시_elastic_search_조회_및_캐시_저장() {
            // given
            given(redisTemplate.hasKey(anyString())).willReturn(false);
            given(redisTemplate.opsForValue()).willReturn(valueOperations);

            Page<StoreDocument> result = new PageImpl<>(List.of(storeDocument));
            given(storeElasticRepository.searchByKeywordAndCategoryId(null, null, pageable)).willReturn(result);

            // when
            Page<StoreDocumentResponseDto> response = storeSearchService.getStoresV3(authUser, 1, 10, sortField, sortOrder, null, null);

            // then
            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals(response.getContent().get(0).getStoreId(), storeId)
            );
            verify(redisTemplate.opsForValue()).set(anyString(), any());
        }
    }
}
