package org.example.tablenow.domain.rating.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.rating.entity.Rating;

import java.time.LocalDateTime;

@Getter
public class RatingUpdateResponseDto {
    private final Long ratingId;
    private final Long userId;
    private final Long storeId;
    private final Integer rating;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime updatedAt;

    @Builder
    private RatingUpdateResponseDto(Long ratingId, Long userId, Long storeId, Integer rating, LocalDateTime updatedAt) {
        this.ratingId = ratingId;
        this.userId = userId;
        this.storeId = storeId;
        this.rating = rating;
        this.updatedAt = updatedAt;
    }

    public static RatingUpdateResponseDto fromRating(Rating rating) {
        return RatingUpdateResponseDto.builder()
                .ratingId(rating.getId())
                .userId(rating.getUserId())
                .storeId(rating.getStoreId())
                .rating(rating.getRating())
                .updatedAt(rating.getUpdatedAt())
                .build();
    }
}
