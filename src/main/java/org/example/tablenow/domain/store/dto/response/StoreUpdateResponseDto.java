package org.example.tablenow.domain.store.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.store.entity.Store;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
public class StoreUpdateResponseDto {
    private final Long storeId;
    private final String name;
    private final Long userId;
    private final Long categoryId;
    private final String categoryName;
    private final String description;
    private final String address;
    private final String imageUrl;
    private final Integer capacity;
    @JsonFormat(pattern = "HH:mm")
    private final LocalTime startTime;
    @JsonFormat(pattern = "HH:mm")
    private final LocalTime endTime;
    private final Integer deposit;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime updatedAt;

    @Builder
    public StoreUpdateResponseDto(Long storeId, String name, Long userId, Long categoryId, String categoryName, String description, String address, String imageUrl, Integer capacity, LocalTime startTime, LocalTime endTime, Integer deposit, LocalDateTime updatedAt) {
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
        this.updatedAt = updatedAt;
    }

    public static StoreUpdateResponseDto fromStore(Store store) {
        return StoreUpdateResponseDto.builder()
                .storeId(store.getId())
                .name(store.getName())
                .userId(store.getUserId())
                .categoryId(store.getCategoryId())
                .categoryName(store.getCategoryName())
                .description(store.getDescription())
                .address(store.getAddress())
                .imageUrl(store.getImageUrl())
                .capacity(store.getCapacity())
                .startTime(store.getStartTime())
                .endTime(store.getEndTime())
                .deposit(store.getDeposit())
                .updatedAt(store.getUpdatedAt())
                .build();
    }
}
