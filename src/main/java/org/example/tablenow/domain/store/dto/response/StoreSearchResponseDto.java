package org.example.tablenow.domain.store.dto.response;

import lombok.Builder;
import org.example.tablenow.domain.store.entity.Store;

import java.time.LocalTime;

public class StoreSearchResponseDto {
    private final Long storeId;
    private final String name;
    private final Long categoryId;
    private final String categoryName;
    private final String imageUrl;
    private final LocalTime startTime;
    private final LocalTime endTime;

    @Builder
    public StoreSearchResponseDto(Long storeId, String name, Long categoryId, String categoryName, String imageUrl, LocalTime startTime, LocalTime endTime) {
        this.storeId = storeId;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.imageUrl = imageUrl;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public static StoreSearchResponseDto fromStore(Store store) {
        return StoreSearchResponseDto.builder()
                .storeId(store.getId())
                .name(store.getName())
                .categoryId(store.getCategory().getId())
                .categoryName(store.getCategory().getName())
                .imageUrl(store.getImageUrl())
                .startTime(store.getStartTime())
                .endTime(store.getEndTime())
                .build();
    }
}
