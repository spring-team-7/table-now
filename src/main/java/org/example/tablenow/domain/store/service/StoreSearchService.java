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
    private final StoreTextAnalyzerService storeTextAnalyzerService;
    private final StoreService storeService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final JavaType STORE_PAGE_RESPONSE_TYPE =
            new ObjectMapper().getTypeFactory()
                    .constructParametricType(PageResponse.class, StoreDocumentResponseDto.class);

    @Transactional(readOnly = true)
    public PageResponse<StoreDocumentResponseDto> getStoresV3(AuthUser authUser, int page, int size, String sort, String direction, Long categoryId, String keyword) {
        // 정렬 파싱
        Pageable pageable = resolvePageable(page, size, sort, direction);
        String cacheKey = STORE_SEARCH_KEY + StoreKeyGenerator.generateStoreListKey(page, size, sort, direction, categoryId, keyword);

        // 인기 검색어 저장
        storeService.savePopularKeyword(authUser, keyword);

        // Redis 캐시 조회
        PageResponse<StoreDocumentResponseDto> cached = getFromCache(cacheKey);
        if (cached != null) return cached;

        // ElasticSearch 조회
        return fetchFromElasticAndCache(categoryId, keyword, pageable, cacheKey);
    }

    public void evictSearchCacheForNewStore(StoreDocument storeDocument) {
        Set<String> keysToDelete = new HashSet<>();
        keysToDelete.addAll(scanKeysByKeywordTokens(storeDocument.getName()));

        if (!keysToDelete.isEmpty()) {
            stringRedisTemplate.delete(keysToDelete);
            log.info("[Cache Evict] 삭제된 키 수: {}", keysToDelete.size());
        }
    }

    public void evictSearchCacheByStoreId(Long storeId) {
        String indexKey = STORE_CACHE_KEY + storeId;
        Set<String> cacheKeys = stringRedisTemplate.opsForSet().members(indexKey);
        if (!cacheKeys.isEmpty()) {
            stringRedisTemplate.delete(cacheKeys);
            stringRedisTemplate.delete(indexKey);
            log.info("[Cache Evict] storeId {} 관련 {}개 키 삭제", storeId, cacheKeys.size());
        }
    }

    private Pageable resolvePageable(int page, int size, String sort, String direction) {
        try {
            Sort sortOption = Sort.by(Sort.Direction.fromString(direction), StoreSortField.fromString(sort));
            return PageRequest.of(page - 1, size, sortOption);
        } catch (IllegalArgumentException e) {
            throw new HandledException(ErrorCode.INVALID_ORDER_VALUE);
        }
    }

    private PageResponse<StoreDocumentResponseDto> getFromCache(String cacheKey) {
        try {
            String cacheValue = stringRedisTemplate.opsForValue().get(cacheKey);
            if (StringUtils.hasText(cacheValue)) {
                return objectMapper.readValue(cacheValue, STORE_PAGE_RESPONSE_TYPE);
            } else {
                log.debug("[Redis] 캐시 미스 - 키: {}", cacheKey);
            }
        } catch (JsonProcessingException e) {
            log.error("[Redis] Value Json 변환 중 에러 발생", e);
        }
        return null;
    }


    private PageResponse<StoreDocumentResponseDto> fetchFromElasticAndCache(Long categoryId, String keyword, Pageable pageable, String storeKey) {
        Page<StoreDocument> storeDocuments = storeElasticRepository.searchByKeywordAndCategoryId(keyword, categoryId, pageable);
        PageResponse<StoreDocumentResponseDto> response = new PageResponse<>(storeDocuments.map(StoreDocumentResponseDto::fromStoreDocument));
        saveToCache(storeKey, response);
        return response;
    }

    private void saveToCache(String storeKey, PageResponse<StoreDocumentResponseDto> response) {
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

    private Set<String> scanKeysByKeywordTokens(String storeName) {
        Set<String> keys = new HashSet<>();
        Set<String> tokens = storeTextAnalyzerService.analyzeText(STORE_INDEX, STORE_ANALYZER, storeName);

        String emptyKeyword = StoreKeyGenerator.generateStoreKeyByPattern(STORE_SEARCH_KEY, "keyword", "");
        keys.addAll(scanKeys(emptyKeyword));

        for (String token : tokens) {
            String pattern = StoreKeyGenerator.generateStoreKeyByPattern(STORE_SEARCH_KEY, "keyword", "*" + token + "*");
            keys.addAll(scanKeys(pattern));
        }

        return keys;
    }

    private Set<String> scanKeys(String pattern) {
        Set<String> keys = new HashSet<>();
        ScanOptions options = ScanOptions.scanOptions().match(pattern).count(500).build();

        try (
                RedisConnection connection = stringRedisTemplate.getConnectionFactory().getConnection();
                Cursor<byte[]> cursor = connection.scan(options)
        ) {
            StringRedisSerializer stringSerializer = new StringRedisSerializer();
            while (cursor.hasNext()) {
                String key = stringSerializer.deserialize(cursor.next());
                if (key != null) {
                    keys.add(key);
                }
            }
        } catch (Exception e) {
            log.error("[Redis] 키 스캔 실패 - 패턴: {}", pattern, e);
        }

        return keys;
    }
}
