package org.example.tablenow.domain.store.dto.response;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.store.entity.StoreDocument;

import java.io.Serializable;

@Getter
public class StoreDocumentResponseDto implements Serializable {
    private final Long storeId;
    private final String name;
    private final Long categoryId;
    private final String categoryName;
    private final String imageUrl;
    @JsonFormat(pattern = "HH:mm")
    private final String startTime;
    @JsonFormat(pattern = "HH:mm")
    private final String endTime;
    private final Double rating;
    private final Integer ratingCount;

    @Builder
    @JsonCreator
    public StoreDocumentResponseDto(@JsonProperty("storeId") Long storeId,
                                    @JsonProperty("name") String name,
                                    @JsonProperty("categoryId") Long categoryId,
                                    @JsonProperty("categoryName") String categoryName,
                                    @JsonProperty("imageUrl") String imageUrl,
                                    @JsonProperty("startTime") String startTime,
                                    @JsonProperty("endTime") String endTime,
                                    @JsonProperty("rating") Double rating,
                                    @JsonProperty("ratingCount") Integer ratingCount) {
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

    public static StoreDocumentResponseDto fromStoreDocument(StoreDocument storeDocument) {
        return StoreDocumentResponseDto.builder()
                .storeId(storeDocument.getId())
                .name(storeDocument.getName())
                .categoryId(storeDocument.getCategoryId())
                .categoryName(storeDocument.getCategoryName())
                .imageUrl(storeDocument.getImageUrl())
                .startTime(storeDocument.getStartTime())
                .endTime(storeDocument.getEndTime())
                .rating(storeDocument.getRating())
                .ratingCount(storeDocument.getRatingCount())
                .build();
    }
}
