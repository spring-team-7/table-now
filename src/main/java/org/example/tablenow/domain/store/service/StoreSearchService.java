package org.example.tablenow.domain.store.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.tablenow.domain.store.dto.response.StoreDocumentResponseDto;
import org.example.tablenow.domain.store.entity.StoreDocument;
import org.example.tablenow.domain.store.enums.StoreSortField;
import org.example.tablenow.domain.store.repository.StoreElasticRepository;
import org.example.tablenow.domain.store.util.StoreKeyGenerator;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.dto.PageResponse;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.example.tablenow.domain.store.util.StoreConstant.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreSearchService {

    private final StoreElasticRepository storeElasticRepository;
    private final TextAnalyzerService textAnalyzerService;
    private final StoreService storeService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final JavaType STORE_PAGE_RESPONSE_TYPE =
            new ObjectMapper().getTypeFactory()
                    .constructParametricType(PageResponse.class, StoreDocumentResponseDto.class);

    @Transactional(readOnly = true)
    public PageResponse<StoreDocumentResponseDto> getStoresV3(AuthUser authUser, int page, int size, String sort, String direction, Long categoryId, String keyword) {
        try {
            Sort sortOption = Sort.by(Sort.Direction.fromString(direction), StoreSortField.fromString(sort));
            Pageable pageable = PageRequest.of(page - 1, size, sortOption);

            // 인기 검색어 저장
            storeService.savePopularKeyword(authUser, keyword);

            // Redis 조회
            String storeKey = STORE_SEARCH_KEY + StoreKeyGenerator.generateStoreListKey(page, size, sort, direction, categoryId, keyword);
            if (stringRedisTemplate.hasKey(storeKey)) {
                String storeCache = stringRedisTemplate.opsForValue().get(storeKey);
                if (StringUtils.hasText(storeCache)) {
                    try {
                        return objectMapper.readValue(storeCache, STORE_PAGE_RESPONSE_TYPE);
                    } catch (JsonProcessingException e) {
                        log.error("[Redis] Value Json 변환 중 에러 발생", e);
                    }
                }
            }

            // ElasticSearch 조회
            Page<StoreDocument> storeDocuments = storeElasticRepository.searchByKeywordAndCategoryId(keyword, categoryId, pageable);
            PageResponse<StoreDocumentResponseDto> response = new PageResponse<>(storeDocuments.map(StoreDocumentResponseDto::fromStoreDocument));

            if (!response.getContent().isEmpty()) {
                try {
                    // 검색 결과 Redis 캐시 저장
                    stringRedisTemplate.opsForValue().set(storeKey, objectMapper.writeValueAsString(response), 1, TimeUnit.DAYS);
                    // 역인덱스 추가
                    Long[] storeIds = response.getContent().stream().map(StoreDocumentResponseDto::getStoreId).distinct().toArray(Long[]::new);
                    for (Long storeId : storeIds) {
                        stringRedisTemplate.opsForSet().add(STORE_CACHE_KEY + storeId, storeKey);
                        stringRedisTemplate.expire(STORE_CACHE_KEY + storeId, 1, TimeUnit.DAYS);
                    }
                } catch (JsonProcessingException e) {
                    log.error("[Redis] Value Json 변환 중 에러 발생", e);
                }
            }
            return response;
        } catch (IllegalArgumentException e) {
            throw new HandledException(ErrorCode.INVALID_ORDER_VALUE);
        }
    }

    public void invalidateCacheOnNewStore(StoreDocument storeDocument) {
        Set<String> keysToDelete = new HashSet<>();

        // 키워드 검색 조건: 가게명 기준 예상 검색어 분석
        Set<String> keywordTokens = textAnalyzerService.analyzeText(STORE_INDEX_NAME, STORE_ANALYZER_NAME, storeDocument.getName());
        if (!keywordTokens.isEmpty()) {
            for (String keyword : keywordTokens) {
                String pattern = StoreKeyGenerator.generateStoreKeyByPattern(STORE_SEARCH_KEY, "keyword", keyword);
                Set<String> keywordKeys = scanKeys(pattern);
                keysToDelete.addAll(keywordKeys);
            }
        }

        // 카테고리 검색 조건
        String pattern = StoreKeyGenerator.generateStoreKeyByPattern(STORE_SEARCH_KEY, "categoryId", String.valueOf(storeDocument.getCategoryId()));
        Set<String> categoryKeys = scanKeys(pattern);
        keysToDelete.addAll(categoryKeys);

        stringRedisTemplate.delete(keysToDelete);
    }

    private Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(1000).build();
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        RedisConnection connection = stringRedisTemplate.getConnectionFactory().getConnection();

        try (Cursor<byte[]> cursor = connection.scan(options)) {
            while (cursor.hasNext()) {
                byte[] rawKey = cursor.next();
                String key = stringSerializer.deserialize(rawKey);
                if (key != null) {
                    keys.add(key);
                }
            }
        } catch (Exception e) {
            log.error("Redis 키 조회 오류 발생: {}", pattern, e);
        }
        return keys;
    }


    public void invalidateStoreCacheByKey(Long storeId) {
        Set<String> cacheKeys = stringRedisTemplate.opsForSet().members(STORE_CACHE_KEY + storeId);
        if (!cacheKeys.isEmpty()) {
            // 검색 결과 Redis 캐시 삭제
            stringRedisTemplate.delete(cacheKeys);
            // 역인덱스 삭제
            stringRedisTemplate.delete(STORE_CACHE_KEY + storeId);
        }
    }
}
