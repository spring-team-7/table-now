package org.example.tablenow.domain.store.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.store.dto.response.StoreDocumentResponseDto;
import org.example.tablenow.domain.store.entity.StoreDocument;
import org.example.tablenow.domain.store.enums.StoreSortField;
import org.example.tablenow.domain.store.repository.StoreElasticRepository;
import org.example.tablenow.domain.store.util.StoreKeyGenerator;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.data.domain.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class StoreSearchService {

    private final StoreElasticRepository storeElasticRepository;
    private final StoreService storeService;
    private final RedisTemplate<Object, Object> redisTemplate;
    private static final String SEARCH_KEY_PREFIX = "store:search:";

    @Transactional(readOnly = true)
    public Page<StoreDocumentResponseDto> getStoresV3(AuthUser authUser, int page, int size, String sort, String direction, Long categoryId, String keyword) {
        try {
            Sort sortOption = Sort.by(Sort.Direction.fromString(direction), StoreSortField.fromString(sort));
            Pageable pageable = PageRequest.of(page - 1, size, sortOption);

            // 인기 검색어 저장
            storeService.savePopularKeyword(authUser, keyword);

            // Redis 조회
            String storeKey = SEARCH_KEY_PREFIX + StoreKeyGenerator.generateStoreListKey(page, size, sort, direction, categoryId, keyword);
            if (redisTemplate.hasKey(storeKey)) {
                Object storeCache = redisTemplate.opsForValue().get(storeKey);
                return (Page<StoreDocumentResponseDto>) storeCache;
            }

            // ElasticSearch 조회
            Page<StoreDocument> storeDocuments = storeElasticRepository.searchByKeywordAndCategoryId(keyword, categoryId, pageable);
            Page<StoreDocumentResponseDto> response = storeDocuments.map(storeDocument -> StoreDocumentResponseDto.fromStoreDocument(storeDocument));

            if (!response.getContent().isEmpty()) {
                // Redis 캐시 저장
                redisTemplate.opsForValue().set(storeKey, response);
                redisTemplate.expire(storeKey, 1, TimeUnit.DAYS);
            }
            return response;
        } catch (IllegalArgumentException e) {
            throw new HandledException(ErrorCode.INVALID_ORDER_VALUE);
        }
    }
}
