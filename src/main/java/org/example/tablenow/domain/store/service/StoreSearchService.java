package org.example.tablenow.domain.store.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.store.dto.response.StoreDocumentResponseDto;
import org.example.tablenow.domain.store.entity.StoreDocument;
import org.example.tablenow.domain.store.enums.StoreSortField;
import org.example.tablenow.domain.store.repository.StoreElasticRepository;
import org.example.tablenow.domain.store.util.StoreKeyGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class StoreSearchService {

    private final StoreElasticRepository storeElasticRepository;
    private final RedisTemplate<Object, Object> redisTemplate;
    private static final String SEARCH_KEY_PREFIX = "store:search:";

    @Transactional(readOnly = true)
    public Page<StoreDocumentResponseDto> getStoresV3(int page, int size, String sort, String direction, Long categoryId, String keyword) {
        Sort sortOption = Sort.by(Sort.Direction.fromString(direction), StoreSortField.fromString(sort));
        Pageable pageable = PageRequest.of(page - 1, size, sortOption);

        // Redis 조회
        String storeKey = SEARCH_KEY_PREFIX + StoreKeyGenerator.generateStoreListKey(page, size, sort, direction, categoryId, keyword);
        if (redisTemplate.hasKey(storeKey)) {
            Object storeCache = redisTemplate.opsForValue().get(storeKey);
            if (storeCache instanceof Page<?>) {
                return (Page<StoreDocumentResponseDto>) storeCache;
            }
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
    }
}
