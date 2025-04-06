package org.example.tablenow.domain.store.dto.response;

import lombok.Builder;
import org.example.tablenow.domain.store.entity.Store;

import java.time.LocalTime;

public class StoreResponseDto {
    private final Long storeId;
    private final String name;
    private final Long userId;
    private final Long categoryId;
    private final String categoryName;
    private final String description;
    private final String address;
    private final String imageUrl;
    private final int capacity;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final int deposit;

    @Builder
    private StoreResponseDto(Long storeId, String name, Long userId, Long categoryId, String categoryName, String description, String address, String imageUrl, int capacity, LocalTime startTime, LocalTime endTime, int deposit) {
        this.storeId = storeId;
        this.name = name;
        this.userId = userId;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
        this.address = address;
        this.imageUrl = imageUrl;
        this.capacity = capacity;
        this.startTime = startTime;
        this.endTime = endTime;
        this.deposit = deposit;
    }

    public static StoreResponseDto fromStore(Store store) {
        return StoreResponseDto.builder()
                .storeId(store.getId())
                .name(store.getName())
                .userId(store.getUser().getId())
                .categoryId(store.getCategory().getId())
                .categoryName(store.getCategory().getName())
                .description(store.getDescription())
                .address(store.getAddress())
                .imageUrl(store.getImageUrl())
                .capacity(store.getCapacity())
                .startTime(store.getStartTime())
                .endTime(store.getEndTime())
                .deposit(store.getDeposit())
                .build();
    }
}
