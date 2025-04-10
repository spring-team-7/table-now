package org.example.tablenow.domain.store.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.category.entity.Category;
import org.example.tablenow.domain.category.service.CategoryService;
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
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final CategoryService categoryService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final Long MAX_STORES_COUNT = 3L;
    private static final Integer TARGET_HOUR_LENGTH = 10;
    private static final Integer TARGET_DAY_LENGTH = 8;

    @Transactional
    public StoreCreateResponseDto saveStore(AuthUser authUser, StoreCreateRequestDto requestDto) {
        User user = User.fromAuthUser(authUser);

        Category category = categoryService.findCategory(requestDto.getCategoryId());

        validStoreTimes(requestDto.getStartTime(), requestDto.getEndTime());

        Long count = storeRepository.countActiveStoresByUser(user.getId());
        if (count >= MAX_STORES_COUNT) {
            throw new HandledException(ErrorCode.STORE_EXCEED_MAX);
        }

        Store store = Store.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .address(requestDto.getAddress())
                .imageUrl(requestDto.getImageUrl())
                .capacity(requestDto.getCapacity())
                .deposit(requestDto.getDeposit())
                .startTime(requestDto.getStartTime())
                .endTime(requestDto.getEndTime())
                .user(user)
                .category(category)
                .build();

        Store savedStore = storeRepository.save(store);
        return StoreCreateResponseDto.fromStore(savedStore);
    }

    public List<StoreResponseDto> findMyStores(AuthUser authUser) {
        User user = User.fromAuthUser(authUser);
        return storeRepository.findAllByUserId(user.getId());
    }

    @Transactional
    public StoreUpdateResponseDto updateStore(Long id, AuthUser authUser, StoreUpdateRequestDto requestDto) {
        User user = User.fromAuthUser(authUser);

        Store store = getStore(id);

        validStoreOwnerId(store, user);

        Category category = store.getCategory();
        if (requestDto.getCategoryId() != null) {
            category = categoryService.findCategory(requestDto.getCategoryId());
        }

        // 업데이트 필드 구분
        LocalTime startTime = Objects.isNull(requestDto.getStartTime()) ? store.getStartTime() : requestDto.getStartTime();
        LocalTime endTime = Objects.isNull(requestDto.getEndTime()) ? store.getEndTime() : requestDto.getEndTime();
        validStoreTimes(startTime, endTime);

        String name = StringUtils.hasText(requestDto.getName()) ? requestDto.getName() : store.getName();
        String description = StringUtils.hasText(requestDto.getDescription()) ? requestDto.getDescription() : store.getDescription();
        String address = StringUtils.hasText(requestDto.getAddress()) ? requestDto.getAddress() : store.getAddress();
        String imageUrl = StringUtils.hasText(requestDto.getImageUrl()) ? requestDto.getImageUrl() : store.getImageUrl();

        int capacity = Objects.isNull(requestDto.getCapacity()) ? store.getCapacity() : requestDto.getCapacity();
        int deposit = Objects.isNull(requestDto.getDeposit()) ? store.getDeposit() : requestDto.getDeposit();

        store.update(name, description, address, imageUrl, capacity, deposit, startTime, endTime, category);
        return StoreUpdateResponseDto.fromStore(store);
    }

    @Transactional
    public StoreDeleteResponseDto deleteStore(Long id, AuthUser authUser) {
        User user = User.fromAuthUser(authUser);

        Store store = getStore(id);

        validStoreOwnerId(store, user);

        store.delete();
        return StoreDeleteResponseDto.fromStore(store.getId());
    }

    public Page<StoreSearchResponseDto> findAllStores(AuthUser authUser, int page, int size, String sort, String direction, Long categoryId, String search) {
        try {
            Sort sortOption = Sort.by(Sort.Direction.fromString(direction), StoreSortField.fromString(sort));
            Pageable pageable = PageRequest.of(page - 1, size, sortOption);

            if (StringUtils.hasText(search)) {
                // 로그인 사용자 기준 어뷰징 방지
                String keyword = StoreUtils.normalizeKeyword(search);
                String userKey = StoreRedisKey.STORE_KEYWORD_USER_KEY + keyword + ":" + authUser.getId();
                boolean alreadySearched = redisTemplate.hasKey(userKey);

                if (!alreadySearched) {
                    // 사용자별 조회 기록: 12시간 중복 방지
                    redisTemplate.opsForValue().set(userKey, "1", 12, TimeUnit.HOURS);

                    // 시간 단위 랭킹 키 생성
                    String hourKey = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));
                    String rankKey = StoreRedisKey.STORE_KEYWORD_RANK_KEY + ":" + hourKey;

                    // 키워드 랭킹 score 증가
                    redisTemplate.opsForZSet().incrementScore(rankKey, keyword, 1);
                }
            }
            return storeRepository.searchStores(pageable, categoryId, search);
        } catch (IllegalArgumentException e) {
            throw new HandledException(ErrorCode.INVALID_ORDER_VALUE);
        }
    }

    public StoreResponseDto findStore(Long id) {
        return StoreResponseDto.fromStore(getStore(id));
    }

    public List<StoreRankingResponseDto> getStoreRanking(int limit, String timeKey) {
        List<StoreRankingResponseDto> result = new ArrayList<>();

        Map<String, Integer> rankMap;

        String target = StringUtils.hasText(timeKey) ? timeKey : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHH"));

        if (TARGET_HOUR_LENGTH == target.length()) {
            rankMap = getKeywordRankingByHour(target);
        } else if (TARGET_DAY_LENGTH == target.length()) {
            rankMap = getKeywordRankingByDay(target);
        } else {
            throw new HandledException(ErrorCode.STORE_RANKING_TIME_KEY_ERROR);
        }

        if (rankMap.isEmpty()) {
            return result;
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

    private Map<String, Integer> getKeywordRankingByHour(String timeKey) {
        Map<String, Integer> rankMap = new LinkedHashMap<>();

        String rankKey = StoreRedisKey.STORE_KEYWORD_RANK_KEY + ":" + timeKey;
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(rankKey, 0L, -1);

        if (tuples == null || tuples.isEmpty()) {
            return rankMap;
        }

        return tuples.stream()
                .collect(Collectors.toMap(
                        ZSetOperations.TypedTuple::getValue,
                        tuple -> tuple.getScore().intValue()
                ));
    }

    private Map<String, Integer> getKeywordRankingByDay(String timeKey) {
        Map<String, Integer> result = new LinkedHashMap<>();

        for (int hour = 0; hour < 24; hour++) {
            String hourStr = String.format("%02d", hour);
            String rankKey = StoreRedisKey.STORE_KEYWORD_RANK_KEY + ":" + timeKey + hourStr;

            Set<ZSetOperations.TypedTuple<String>> tuples =
                    redisTemplate.opsForZSet().reverseRangeWithScores(rankKey, 0, -1);

            if (tuples == null) {
                continue;
            }

            for (ZSetOperations.TypedTuple<String> tuple : tuples) {
                String keyword = tuple.getValue();
                Integer score = tuple.getScore().intValue();
                result.merge(keyword, score, Integer::sum);
            }
        }
        return result;
    }

    public Store getStore(Long id) {
        return storeRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new HandledException(ErrorCode.STORE_NOT_FOUND));
    }

    public void validStoreOwnerId(Store store, User user) {
        // 요청한 유저 ID가 가게 주인인지 확인
        if (!store.getUser().getId().equals(user.getId())) {
            throw new HandledException(ErrorCode.STORE_FORBIDDEN);
        }
    }

    private void validStoreTimes(LocalTime startTime, LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new HandledException(ErrorCode.STORE_BAD_REQUEST_TIME);
        }
    }
}
