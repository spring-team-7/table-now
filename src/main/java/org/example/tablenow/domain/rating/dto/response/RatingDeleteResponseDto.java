package org.example.tablenow.domain.rating.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RatingDeleteResponseDto {
    private final Long ratingId;
    private final String message;

    public RatingDeleteResponseDto(Long ratingId, String message) {
        this.ratingId = ratingId;
        this.message = message;
    }

    public static RatingDeleteResponseDto fromRating(Long ratingId) {
        return RatingDeleteResponseDto.builder()
                .ratingId(ratingId)
                .message("삭제되었습니다.")
                .build();
    }
}
