package org.example.tablenow.domain.store.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.store.entity.Store;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Getter
public class StoreCreateResponseDto {
    private final Long storeId;
    private final String name;
    private final Long categoryId;
    private final String categoryName;
    private final String description;
    private final String address;
    private final String imageUrl;
    private final int capacity;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final int deposit;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt;

    @Builder
    private StoreCreateResponseDto(Long storeId, String name, Long categoryId, String categoryName, String description, String address, String imageUrl, int capacity, LocalTime startTime, LocalTime endTime, int deposit, LocalDateTime createdAt) {
        this.storeId = storeId;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.description = description;
        this.address = address;
        this.imageUrl = imageUrl;
        this.capacity = capacity;
        this.startTime = startTime;
        this.endTime = endTime;
        this.deposit = deposit;
        this.createdAt = createdAt;
    }

    public static StoreCreateResponseDto fromStore(Store store) {
        return StoreCreateResponseDto.builder()
                .storeId(store.getId())
                .name(store.getName())
                .categoryId(store.getCategory().getId())
                .categoryName(store.getCategory().getName())
                .description(store.getDescription())
                .address(store.getAddress())
                .imageUrl(store.getImageUrl())
                .capacity(store.getCapacity())
                .startTime(store.getStartTime())
                .endTime(store.getEndTime())
                .deposit(store.getDeposit())
                .createdAt(store.getCreatedAt())
                .build();
    }
}
