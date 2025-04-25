package org.example.tablenow.domain.store.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.tablenow.domain.category.entity.Category;
import org.example.tablenow.domain.store.dto.response.StoreDocumentResponseDto;
import org.example.tablenow.domain.store.entity.StoreDocument;
import org.example.tablenow.domain.store.enums.StoreSortField;
import org.example.tablenow.domain.store.repository.StoreElasticRepository;
import org.example.tablenow.domain.store.util.StoreKeyGenerator;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.domain.user.enums.UserRole;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.dto.PageResponse;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.example.tablenow.domain.store.util.StoreConstant.STORE_CACHE_KEY;
import static org.example.tablenow.domain.store.util.StoreConstant.STORE_SEARCH_KEY;
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
    private TextAnalyzerService textAnalyzerService;
    @Mock
    private StoreService storeService;

    @Mock
    private StringRedisTemplate stringRedisTemplate;
    @Mock
    private ValueOperations<String, String> valueOperations;
    @Mock
    private SetOperations<String, String> setOperations;
    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RedisConnectionFactory redisConnectionFactory;
    @Mock
    private RedisConnection redisConnection;
    @Mock
    private Cursor<byte[]> cursor;

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
        void 가게_검색_cache_hit_시_Redis_캐시값을_파싱하여_반환_성공() throws JsonProcessingException {
            // given
            PageResponse<StoreDocumentResponseDto> expectedResponse = new PageResponse<>(new PageImpl<>(List.of(StoreDocumentResponseDto.fromStoreDocument(storeDocument))));
            // template.opsForValue().get(key) 결과
            String cachedJson = "{\"content\":[{\"storeId\":1,\"name\":\"맛있는 가게\",\"categoryId\":1,\"categoryName\":\"한식\",\"imageUrl\":null,\"startTime\":\"09:00\",\"endTime\":\"21:30\",\"rating\":4.5,\"ratingCount\":100}],\"totalElements\":1}";

            given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
            given(valueOperations.get(anyString())).willReturn(cachedJson);
            given(objectMapper.readValue(anyString(), any(JavaType.class))).willReturn(expectedResponse);

            // when
            PageResponse<StoreDocumentResponseDto> response = storeSearchService.getStoresV3(authUser, 1, 10, sortField, sortOrder, null, null);

            // then
            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals(response.getContent().get(0).getStoreId(), storeId)
            );
        }

        @Test
        void 가게_검색_cache_miss_시_elastic_search_조회_결과_없음() {
            // given
            given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);

            Page<StoreDocument> result = new PageImpl<>(Collections.emptyList());
            given(storeElasticRepository.searchByKeywordAndCategoryId(anyString(), anyLong(), any(Pageable.class))).willReturn(result);

            // when
            PageResponse<StoreDocumentResponseDto> response = storeSearchService.getStoresV3(authUser, 1, 10, sortField, sortOrder, 1L, "test");

            // then
            assertNotNull(response);
            verify(stringRedisTemplate.opsForValue(), never()).set(anyString(), any());
        }

        @Test
        void 가게_검색_cache_miss_시_elastic_search_조회_및_캐시_저장() {
            // given
            given(stringRedisTemplate.opsForValue()).willReturn(valueOperations);
            given(stringRedisTemplate.opsForSet()).willReturn(setOperations);

            Page<StoreDocument> result = new PageImpl<>(List.of(storeDocument));
            given(storeElasticRepository.searchByKeywordAndCategoryId(null, null, pageable)).willReturn(result);

            // when
            PageResponse<StoreDocumentResponseDto> response = storeSearchService.getStoresV3(authUser, 1, 10, sortField, sortOrder, null, null);

            // then
            assertAll(
                    () -> assertNotNull(response),
                    () -> assertEquals(response.getContent().get(0).getStoreId(), storeId)
            );
            verify(stringRedisTemplate.opsForValue()).set(anyString(), isNull(), anyLong(), any(TimeUnit.class));
            verify(stringRedisTemplate.opsForSet()).add(anyString(), anyString());
            verify(stringRedisTemplate).expire(anyString(), anyLong(), any(TimeUnit.class));
        }
    }

    @Nested
    class 가게_등록_시_캐시_무효화 {
        @Test
        void 가게명_기준_예상_검색어_호출_실패_삭제_키_없음() {
            // given
            Set<String> analyzeResult = new HashSet<>();
            given(textAnalyzerService.analyzeText(anyString(), anyString(), anyString())).willReturn(analyzeResult);
            given(stringRedisTemplate.getConnectionFactory()).willReturn(redisConnectionFactory);

            // when
            storeSearchService.evictSearchCacheForNewStore(storeDocument);

            // then
            verify(stringRedisTemplate, never()).delete(Collections.emptySet());
        }

        @Test
        void 가게명_기준_예상_검색어_호출_키_삭제_성공() {
            // given
            Set<String> analyzeResult = Set.of("맛있", "는");
            String key1 = STORE_SEARCH_KEY + StoreKeyGenerator.generateStoreListKey(1, 10, "ratingCount", "desc", null, "맛있");
            String key2 = STORE_SEARCH_KEY + StoreKeyGenerator.generateStoreListKey(1, 10, "ratingCount", "desc", null, "는");

            String serializedKey1 = new StringRedisSerializer().deserialize(key1.getBytes());
            String serializedKey2 = new StringRedisSerializer().deserialize(key2.getBytes());
            Set<String> scanResult = Set.of(serializedKey1, serializedKey2);

            given(textAnalyzerService.analyzeText(anyString(), anyString(), anyString())).willReturn(analyzeResult);

            given(stringRedisTemplate.getConnectionFactory()).willReturn(redisConnectionFactory);
            given(redisConnectionFactory.getConnection()).willReturn(redisConnection);
            given(redisConnection.scan(any(ScanOptions.class))).willReturn(cursor);
            given(cursor.hasNext()).willReturn(true, true, false);
            given(cursor.next()).willReturn(key1.getBytes(), key2.getBytes());

            // when
            storeSearchService.evictSearchCacheForNewStore(storeDocument);

            // then
            verify(stringRedisTemplate).delete(scanResult);
        }

        @Test
        void 카테고리_ID_기준_키_삭제_성공() {
            // given
            String key1 = STORE_SEARCH_KEY + StoreKeyGenerator.generateStoreListKey(1, 10, "ratingCount", "desc", 1L, null);
            String key2 = STORE_SEARCH_KEY + StoreKeyGenerator.generateStoreListKey(1, 10, "rating", "desc", 1L, null);

            String serializedKey1 = new StringRedisSerializer().deserialize(key1.getBytes());
            String serializedKey2 = new StringRedisSerializer().deserialize(key2.getBytes());
            Set<String> scanResult = Set.of(serializedKey1, serializedKey2);

            given(textAnalyzerService.analyzeText(anyString(), anyString(), anyString())).willReturn(Collections.emptySet());

            given(stringRedisTemplate.getConnectionFactory()).willReturn(redisConnectionFactory);
            given(redisConnectionFactory.getConnection()).willReturn(redisConnection);
            given(redisConnection.scan(any(ScanOptions.class))).willReturn(cursor);
            given(cursor.hasNext()).willReturn(true, true, false);
            given(cursor.next()).willReturn(key1.getBytes(), key2.getBytes());

            // when
            storeSearchService.evictSearchCacheForNewStore(storeDocument);

            // then
            verify(stringRedisTemplate).delete(scanResult);
        }
    }

    @Nested
    class 가게_수정_또는_삭제_시_캐시_무효화 {
        String invertedIndexKey = STORE_CACHE_KEY + storeId;

        @Test
        void 역인덱스_캐시_키가_없을_경우_무효화_작업_스킵() {
            // given
            Set<String> cacheResult = new HashSet<>();
            given(stringRedisTemplate.opsForSet()).willReturn(setOperations);
            given(setOperations.members(anyString())).willReturn(cacheResult);

            // when
            storeSearchService.evictSearchCacheByStoreId(storeId);

            // then
            verify(stringRedisTemplate, never()).delete(cacheResult);
            verify(stringRedisTemplate, never()).delete(invertedIndexKey);
        }

        @Test
        void 역인덱스_캐시_키_조회_캐시_무효화_및_역인덱스_삭제() {
            // given
            String cachedKey = "store:search:page=1:size=10:sort=rating:direction=desc:categoryId=1:keyword=";

            Set<String> cacheResult = Set.of(cachedKey);

            given(stringRedisTemplate.opsForSet()).willReturn(setOperations);
            given(setOperations.members(anyString())).willReturn(cacheResult);

            // when
            storeSearchService.evictSearchCacheByStoreId(storeId);

            // then
            verify(stringRedisTemplate).delete(cacheResult);
            verify(stringRedisTemplate).delete(invertedIndexKey);
        }

    }
}
