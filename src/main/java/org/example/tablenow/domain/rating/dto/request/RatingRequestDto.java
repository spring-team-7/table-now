package org.example.tablenow.domain.rating.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RatingRequestDto {
    @NotNull(message = "평점은 필수값입니다.")
    @Min(value = 1, message = "평점은 1점 미만일 수 없습니다.")
    @Max(value = 5, message = "평점은 5점 초과일 수 없습니다.")
    private Integer rating;
}
