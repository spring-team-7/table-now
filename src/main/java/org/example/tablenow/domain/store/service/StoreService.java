package org.example.tablenow.domain.store.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.category.entity.Category;
import org.example.tablenow.domain.category.service.CategoryService;
import org.example.tablenow.domain.image.service.ImageService;
import org.example.tablenow.domain.store.dto.request.StoreCreateRequestDto;
import org.example.tablenow.domain.store.dto.request.StoreUpdateRequestDto;
import org.example.tablenow.domain.store.dto.response.*;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.enums.StoreSortField;
import org.example.tablenow.domain.store.repository.StoreRepository;
import org.example.tablenow.domain.store.util.StoreRedisKey;
import org.example.tablenow.domain.store.util.StoreUtils;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.global.dto.AuthUser;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.HandledException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StoreService {

    private final StoreRepository storeRepository;
    private final CategoryService categoryService;
    private final ImageService imageService;
    private final RedisTemplate<Object, Object> redisTemplate;

    private static final Long MAX_STORES_COUNT = 3L;
    private static final Integer TARGET_HOUR_LENGTH = 10;
    private static final Integer TARGET_DAY_LENGTH = 8;
    private static final DateTimeFormatter TIME_KEY_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHH");

    @CacheEvict(value = "stores", allEntries = true)
    @Transactional
    public StoreCreateResponseDto createStore(AuthUser authUser, StoreCreateRequestDto request) {
        User user = User.fromAuthUser(authUser);

        Category category = categoryService.findCategory(request.getCategoryId());

        validateStartTimeIsBeforeEndTime(request.getStartTime(), request.getEndTime());

        Long count = storeRepository.countActiveStoresByUser(user.getId());
        if (count >= MAX_STORES_COUNT) {
            throw new HandledException(ErrorCode.STORE_EXCEED_MAX);
        }

        Store store = Store.builder()
                .name(request.getName())
                .description(request.getDescription())
                .address(request.getAddress())
                .imageUrl(request.getImageUrl())
                .capacity(request.getCapacity())
                .deposit(request.getDeposit())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .user(user)
                .category(category)
                .build();

        Store savedStore = storeRepository.save(store);
        return StoreCreateResponseDto.fromStore(savedStore);
    }

    @Transactional(readOnly = true)
    public List<StoreResponseDto> findMyStores(AuthUser authUser) {
        User user = User.fromAuthUser(authUser);
        return storeRepository.findAllByUserId(user.getId());
    }

    @CacheEvict(value = "stores", allEntries = true)
    @Transactional
    public StoreUpdateResponseDto updateStore(Long id, AuthUser authUser, StoreUpdateRequestDto request) {
        User user = User.fromAuthUser(authUser);
        Store store = getStore(id);
        validateStoreOwnerId(store, user);
        validateUpdateStoreTime(request, store);

        if (request.getCategoryId() != null) {
            Category category = categoryService.findCategory(request.getCategoryId());
            store.updateCategory(category);
        }

        if (request.getStartTime() != null) {
            store.updateStartTime(request.getStartTime());
        }

        if (request.getEndTime() != null) {
            store.updateEndTime(request.getEndTime());
        }

        if (StringUtils.hasText(request.getName())) {
            store.updateName(request.getName());
        }

        if (StringUtils.hasText(request.getDescription())) {
            store.updateDescription(request.getDescription());
        }

        if (StringUtils.hasText(request.getAddress())) {
            store.updateAddress(request.getAddress());
        }

        String storeImageUrl = store.getImageUrl();
        String requestImageUrl = request.getImageUrl();
        if (StringUtils.hasText(requestImageUrl)) {
            if (!Objects.equals(requestImageUrl, storeImageUrl) && StringUtils.hasText(storeImageUrl)) {
                imageService.delete(storeImageUrl);
            }
            store.updateImageUrl(requestImageUrl);
        }

        if (request.getCapacity() != null) {
            store.updateCapacity(request.getCapacity());
        }

        if (request.getDeposit() != null) {
            store.updateDeposit(request.getDeposit());
        }

        return StoreUpdateResponseDto.fromStore(store);
    }

    @CacheEvict(value = "stores", allEntries = true)
    @Transactional
    public StoreDeleteResponseDto deleteStore(Long id, AuthUser authUser) {
        User user = User.fromAuthUser(authUser);

        Store store = getStore(id);

        validateStoreOwnerId(store, user);

        if (StringUtils.hasText(store.getImageUrl())) {
            imageService.delete(store.getImageUrl());
        }

        store.deleteStore();
        return StoreDeleteResponseDto.fromStore(store.getId());
    }

    @Transactional(readOnly = true)
    public Page<StoreSearchResponseDto> getStoresV1(AuthUser authUser, int page, int size, String sort, String direction, Long categoryId, String keyword) {
        return findAllStores(authUser, page, size, sort, direction, categoryId, keyword);
    }

    @Cacheable(
            value = "stores",
            key = "T(org.example.tablenow.domain.store.util.StoreKeyGenerator).generateStoreListKey(#page, #size, #sort, #direction, #categoryId, #keyword)"
    )
    @Transactional(readOnly = true)
    public Page<StoreSearchResponseDto> getStoresV2(AuthUser authUser, int page, int size, String sort, String direction, Long categoryId, String keyword) {
        return findAllStores(authUser, page, size, sort, direction, categoryId, keyword);
    }

    @Transactional(readOnly = true)
    public StoreResponseDto findStore(Long id) {
        return StoreResponseDto.fromStore(getStore(id));
    }

    @Transactional(readOnly = true)
    public List<StoreRankingResponseDto> getStoreRanking(int limit, String timeKey) {
        Map<String, Integer> rankMap;

        String target = validateTimeKey(timeKey);

        rankMap = getRankMapByTimeKey(target);

        if (rankMap.isEmpty()) {
            return Collections.emptyList();
        }

        AtomicInteger rank = new AtomicInteger(1);
        return rankMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(limit)
                .map(entry -> StoreRankingResponseDto.builder()
                        .rank(rank.getAndIncrement())
                        .keyword(entry.getKey())
                        .score(entry.getValue())
                        .build())
                .toList();
    }

    public Store getStore(Long id) {
        return storeRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new HandledException(ErrorCode.STORE_NOT_FOUND));
    }

    private Page<StoreSearchResponseDto> findAllStores(AuthUser authUser, int page, int size, String sort, String direction, Long categoryId, String keyword) {
        try {
            Sort sortOption = Sort.by(Sort.Direction.fromString(direction), StoreSortField.fromString(sort));
            Pageable pageable = PageRequest.of(page - 1, size, sortOption);

            savePopularKeyword(authUser, keyword);
            return storeRepository.searchStores(pageable, categoryId, keyword);
        } catch (IllegalArgumentException e) {
            throw new HandledException(ErrorCode.INVALID_ORDER_VALUE);
        }
    }

    public void savePopularKeyword(AuthUser authUser, String keyword) {
        if (authUser != null && StringUtils.hasText(keyword)) {
            // 로그인 사용자 기준 어뷰징 방지
            String normalizeKeyword = StoreUtils.normalizeKeyword(keyword);
            String userKey = StoreRedisKey.STORE_KEYWORD_USER_KEY + normalizeKeyword + ":" + authUser.getId();
            boolean alreadySearched = redisTemplate.hasKey(userKey);

            if (!alreadySearched) {
                // 사용자별 조회 기록: 1일 중복 방지
                redisTemplate.opsForValue().set(userKey, "1", 1, TimeUnit.DAYS);

                // 시간 단위 랭킹 키 생성
                String hourKey = LocalDateTime.now().format(TIME_KEY_FORMATTER);
                String rankKey = StoreRedisKey.STORE_KEYWORD_RANK_KEY + ":" + hourKey;

                // 키워드 랭킹 score 증가
                redisTemplate.opsForZSet().incrementScore(rankKey, normalizeKeyword, 1);
                // 인기 검색어 TTL 1일 설정
                redisTemplate.expire(rankKey, 1, TimeUnit.DAYS);
            }
        }
    }

    public void validateStoreOwnerId(Store store, User user) {
        // 요청한 유저 ID가 가게 주인인지 확인
        if (!store.getUser().getId().equals(user.getId())) {
            throw new HandledException(ErrorCode.STORE_FORBIDDEN);
        }
    }

    private void validateUpdateStoreTime(StoreUpdateRequestDto request, Store store) {
        LocalTime startTime = request.getStartTime() != null ? request.getStartTime() : store.getStartTime();
        LocalTime endTime = request.getEndTime() != null ? request.getEndTime() : store.getEndTime();
        validateStartTimeIsBeforeEndTime(startTime, endTime);
    }

    private void validateStartTimeIsBeforeEndTime(LocalTime startTime, LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new HandledException(ErrorCode.STORE_BAD_REQUEST_TIME);
        }
    }

    private String validateTimeKey(String timeKey) {
        return StringUtils.hasText(timeKey) ? timeKey : LocalDateTime.now().format(TIME_KEY_FORMATTER);
    }

    private Map<String, Integer> getRankMapByTimeKey(String timeKey) {
        if (TARGET_HOUR_LENGTH == timeKey.length()) {
            return getKeywordRankingByHour(timeKey);
        } else if (TARGET_DAY_LENGTH == timeKey.length()) {
            return getKeywordRankingByDay(timeKey);
        } else {
            throw new HandledException(ErrorCode.STORE_RANKING_TIME_KEY_ERROR);
        }
    }

    private Map<String, Integer> getKeywordRankingByHour(String timeKey) {
        Map<String, Integer> rankMap = new LinkedHashMap<>();

        String rankKey = StoreRedisKey.STORE_KEYWORD_RANK_KEY + ":" + timeKey;
        Set<ZSetOperations.TypedTuple<Object>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(rankKey, 0L, -1);

        if (tuples.isEmpty()) {
            return rankMap;
        }

        return tuples.stream()
                .collect(Collectors.toMap(
                        tuple -> tuple.getValue().toString(),
                        tuple -> tuple.getScore().intValue()
                ));
    }

    private Map<String, Integer> getKeywordRankingByDay(String timeKey) {
        Map<String, Integer> result = new LinkedHashMap<>();

        for (int hour = 0; hour < 24; hour++) {
            String hourStr = String.format("%02d", hour);
            String rankKey = StoreRedisKey.STORE_KEYWORD_RANK_KEY + ":" + timeKey + hourStr;

            Set<ZSetOperations.TypedTuple<Object>> tuples =
                    redisTemplate.opsForZSet().reverseRangeWithScores(rankKey, 0, -1);

            if (tuples.isEmpty()) {
                continue;
            }

            for (ZSetOperations.TypedTuple<Object> tuple : tuples) {
                String keyword = tuple.getValue().toString();
                Integer score = tuple.getScore().intValue();
                result.merge(keyword, score, Integer::sum);
            }
        }
        return result;
    }
}
