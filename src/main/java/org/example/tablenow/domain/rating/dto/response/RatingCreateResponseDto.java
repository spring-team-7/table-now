package org.example.tablenow.domain.rating.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;
import org.example.tablenow.domain.rating.entity.Rating;

import java.time.LocalDateTime;

@Getter
public class RatingCreateResponseDto {
    private final Long ratingId;
    private final Long userId;
    private final Long storeId;
    private final Integer rating;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private final LocalDateTime createdAt;

    @Builder
    private RatingCreateResponseDto(Long ratingId, Long userId, Long storeId, Integer rating, LocalDateTime createdAt) {
        this.ratingId = ratingId;
        this.userId = userId;
        this.storeId = storeId;
        this.rating = rating;
        this.createdAt = createdAt;
    }

    public static RatingCreateResponseDto fromRating(Rating rating) {
        return RatingCreateResponseDto.builder()
                .ratingId(rating.getId())
                .userId(rating.getUserId())
                .storeId(rating.getStoreId())
                .rating(rating.getRating())
                .createdAt(rating.getCreatedAt())
                .build();
    }
}
