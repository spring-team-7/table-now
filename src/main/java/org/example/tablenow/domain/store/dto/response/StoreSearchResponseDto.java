package org.example.tablenow.domain.store.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.store.entity.Store;

import java.io.Serializable;
import java.time.LocalTime;

@Getter
public class StoreSearchResponseDto implements Serializable {
    private final Long storeId;
    private final String name;
    private final Long categoryId;
    private final String categoryName;
    private final String imageUrl;
    private final LocalTime startTime;
    private final LocalTime endTime;
    private final Double averageRating;
    private final Integer ratingCount;

    @Builder
    public StoreSearchResponseDto(Long storeId, String name, Long categoryId, String categoryName, String imageUrl, LocalTime startTime, LocalTime endTime, Double averageRating, Integer ratingCount) {
        this.storeId = storeId;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.imageUrl = imageUrl;
        this.startTime = startTime;
        this.endTime = endTime;
        this.averageRating = averageRating;
        this.ratingCount = ratingCount;
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
                .averageRating(store.getAverageRating())
                .ratingCount(store.getRatingCount())
                .build();
    }
}
