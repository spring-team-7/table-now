package org.example.tablenow.domain.store.dto.request;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.image.annotation.ImageUrlPattern;
import org.example.tablenow.global.annotation.HalfHourOnly;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreUpdateRequestDto {
    private String name;
    private String description;
    private String address;
    @ImageUrlPattern
    private String imageUrl;
    @Min(value = 0, message = "수용테이블은 0 미만일 수 없습니다.")
    private Integer capacity;
    @HalfHourOnly // 00분 또는 30분 단위
    private LocalTime startTime;
    @HalfHourOnly
    private LocalTime endTime;
    @Min(value = 0, message = "예약금은 0 미만일 수 없습니다.")
    private Integer deposit;
    private Long categoryId;
}
