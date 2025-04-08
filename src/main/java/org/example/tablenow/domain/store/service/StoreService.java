package org.example.tablenow.domain.store.service;

import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.category.entity.Category;
import org.example.tablenow.domain.category.service.CategoryService;
import org.example.tablenow.domain.store.dto.request.StoreCreateRequestDto;
import org.example.tablenow.domain.store.dto.request.StoreUpdateRequestDto;
import org.example.tablenow.domain.store.dto.response.*;
import org.example.tablenow.domain.store.entity.Store;
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

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final CategoryService categoryService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final Long MAX_STORES_COUNT = 3L;

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

        storeRepository.delete(store);
        return StoreDeleteResponseDto.fromStore(store.getId());
    }

    public Page<StoreSearchResponseDto> findAllStores(int page, int size, String sort, String direction, Long categoryId, String search) {
        Sort sortOption = Sort.by(Sort.Direction.fromString(direction), sort);
        Pageable pageable = PageRequest.of(page - 1, size, sortOption);

        // TODO 사용자 기준 어뷰징 방지
        if (StringUtils.hasText(search)) {
            String keyword = StoreUtils.normalizeKeyword(search);
            redisTemplate.opsForZSet().incrementScore(StoreRedisKey.STORE_RANK_KEY, keyword, 1);
        }
        return storeRepository.searchStores(pageable, categoryId, search);
    }

    public StoreResponseDto findStore(Long id) {
        return StoreResponseDto.fromStore(getStore(id));
    }

    public List<StoreRankingResponseDto> getStoreRanking(int limit) {
        List<StoreRankingResponseDto> result = new ArrayList<>();
        Set<ZSetOperations.TypedTuple<String>> tuples = redisTemplate.opsForZSet()
                .reverseRangeWithScores(StoreRedisKey.STORE_RANK_KEY, 0L, limit - 1); // 0~9

        if (tuples == null || tuples.isEmpty()) {
            return result;
        }

        int rank = 1;
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            result.add(StoreRankingResponseDto.builder()
                    .rank(rank++)
                    .keyword(tuple.getValue())
                    .score(tuple.getScore().intValue())
                    .build());
        }
        return result;
    }

    public Store getStore(Long id) {
        return storeRepository.findById(id)
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
            throw new HandledException(ErrorCode.BAD_REQUEST);
        }
    }
}
