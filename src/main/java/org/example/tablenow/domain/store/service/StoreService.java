package org.example.tablenow.domain.store.service;

import io.micrometer.common.util.StringUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.category.entity.Category;
import org.example.tablenow.domain.category.service.CategoryService;
import org.example.tablenow.domain.store.dto.request.StoreCreateRequestDto;
import org.example.tablenow.domain.store.dto.request.StoreUpdateRequestDto;
import org.example.tablenow.domain.store.dto.response.*;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.repository.StoreRepository;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.global.exception.AuthorizationException;
import org.example.tablenow.global.exception.BadRequestException;
import org.example.tablenow.global.exception.ErrorCode;
import org.example.tablenow.global.exception.NotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreService {

    private final StoreRepository storeRepository;
    private final CategoryService categoryService;

    @Transactional
    public StoreCreateResponseDto saveStore(@Valid StoreCreateRequestDto requestDto) {
        // TODO AuthUser -> User
        User user = new User();

        Category category = categoryService.findCategory(requestDto.getCategoryId());

        validStoreTimes(requestDto.getStartTime(), requestDto.getEndTime());

        Long count = storeRepository.countActiveStoresByUser(user.getId());
        if (count == 3) {
            throw new BadRequestException("최대 등록 가게 수를 초과하였습니다.");
        }

        Store store = Store.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .address(requestDto.getAddress())
                .imageUrl(requestDto.getImageUrl())
                .capacity(requestDto.getCapacity())
                .startTime(requestDto.getStartTime())
                .endTime(requestDto.getEndTime())
                .user(user)
                .category(category)
                .build();

        Store savedStore = storeRepository.save(store);
        return StoreCreateResponseDto.fromStore(savedStore);
    }

    public List<StoreResponseDto> findMyStores() {
        // TODO AuthUser -> User
        User user = new User();
        List<Store> stores = storeRepository.findAllByUserId(user.getId());
        return stores.stream().map(StoreResponseDto::fromStore).collect(Collectors.toList());
    }

    @Transactional
    public StoreUpdateResponseDto updateStore(Long id, @Valid StoreUpdateRequestDto requestDto) {
        // TODO AuthUser -> User
        User user = new User();

        Store store = findStore(id);

        validStoreOwnerId(store, user);

        Category category = store.getCategory();
        if (requestDto.getCategoryId() != null) {
            category = categoryService.findCategory(requestDto.getCategoryId());
        }

        // 업데이트 필드 구분
        LocalTime startTime = Objects.isNull(requestDto.getStartTime()) ? store.getStartTime() : requestDto.getStartTime();
        LocalTime endTime = Objects.isNull(requestDto.getEndTime()) ? store.getEndTime() : requestDto.getEndTime();

        validStoreTimes(startTime, endTime);

        String name = StringUtils.isEmpty(requestDto.getName()) ? store.getName() : requestDto.getName();
        String description = StringUtils.isEmpty(requestDto.getDescription()) ? store.getDescription() : requestDto.getDescription();
        String address = StringUtils.isEmpty(requestDto.getAddress()) ? store.getAddress() : requestDto.getAddress();
        String imageUrl = StringUtils.isEmpty(requestDto.getImageUrl()) ? store.getImageUrl() : store.getImageUrl();

        int capacity = Objects.isNull(requestDto.getCapacity()) ? store.getCapacity() : requestDto.getCapacity();
        int deposit = Objects.isNull(requestDto.getDeposit()) ? store.getDeposit() : requestDto.getDeposit();

        store.update(name, description, address, imageUrl, capacity, deposit, startTime, endTime, category);
        return StoreUpdateResponseDto.fromStore(store);
    }

    @Transactional
    public StoreDeleteResponseDto deleteStore(Long id) {
        // TODO AuthUser -> User
        User user = new User();

        Store store = findStore(id);

        validStoreOwnerId(store, user);

        storeRepository.delete(store);
        return StoreDeleteResponseDto.fromStore(store.getId());
    }

    public Page<StoreSearchResponseDto> findAllStores(int page, int size, String sort, String direction, Long categoryId, String search) {
        Sort sortOption = Sort.by(Sort.Direction.fromString(direction), sort);
        Pageable pageable = PageRequest.of(page - 1, size, sortOption);
        return storeRepository.searchStores(pageable, categoryId, search);
    }

    public Store findStore(Long id) {
        return storeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND.getDefaultMessage()));
    }

    private void validStoreOwnerId(Store store, User user) {
        // 요청한 유저 ID가 가게 주인인지 확인
        if (!store.getUser().getId().equals(user.getId())) {
            throw new AuthorizationException(ErrorCode.AUTHORIZATION.getDefaultMessage());
        }
    }

    private void validStoreTimes(LocalTime startTime, LocalTime endTime) {
        if (!startTime.isBefore(endTime)) {
            throw new BadRequestException("시작시간은 종료시간보다 이전이어야 합니다.");
        }
    }
}
