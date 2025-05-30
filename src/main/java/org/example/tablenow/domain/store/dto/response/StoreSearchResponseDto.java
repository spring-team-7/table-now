package org.example.tablenow.domain.store.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.store.entity.Store;

import java.io.Serializable;
import java.time.LocalTime;

import static org.example.tablenow.global.constant.TimeConstants.TIME_HH_MM;

@Getter
public class StoreSearchResponseDto implements Serializable {
    private final Long storeId;
    private final String name;
    private final Long categoryId;
    private final String categoryName;
    private final String imageUrl;
    @JsonFormat(pattern = TIME_HH_MM)
    private final LocalTime startTime;
    @JsonFormat(pattern = TIME_HH_MM)
    private final LocalTime endTime;
    private final Double rating;
    private final Integer ratingCount;

    @Builder
    public StoreSearchResponseDto(Long storeId, String name, Long categoryId, String categoryName, String imageUrl, LocalTime startTime, LocalTime endTime, Double rating, Integer ratingCount) {
        this.storeId = storeId;
        this.name = name;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.imageUrl = imageUrl;
        this.startTime = startTime;
        this.endTime = endTime;
        this.rating = rating;
        this.ratingCount = ratingCount;
    }

    public static StoreSearchResponseDto fromStore(Store store) {
        return StoreSearchResponseDto.builder()
                .storeId(store.getId())
                .name(store.getName())
                .categoryId(store.getCategoryId())
                .categoryName(store.getCategoryName())
                .imageUrl(store.getImageUrl())
                .startTime(store.getStartTime())
                .endTime(store.getEndTime())
                .rating(store.getRating())
                .ratingCount(store.getRatingCount())
                .build();
    }
}
