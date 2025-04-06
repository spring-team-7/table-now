package org.example.tablenow.domain.store.service;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.tablenow.domain.category.entity.Category;
import org.example.tablenow.domain.category.service.CategoryService;
import org.example.tablenow.domain.store.dto.request.StoreCreateRequestDto;
import org.example.tablenow.domain.store.dto.response.StoreCreateResponseDto;
import org.example.tablenow.domain.store.dto.response.StoreResponseDto;
import org.example.tablenow.domain.store.entity.Store;
import org.example.tablenow.domain.store.repository.StoreRepository;
import org.example.tablenow.domain.user.entity.User;
import org.example.tablenow.global.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

        if (!requestDto.getStartTime().isBefore(requestDto.getEndTime())) {
            throw new BadRequestException("시작시간은 종료시간보다 이전이어야 합니다.");
        }

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
}
