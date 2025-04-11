package org.example.tablenow.domain.store.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.image.annotation.ImageUrlPattern;
import org.example.tablenow.domain.store.annotation.HalfHourOnly;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class StoreCreateRequestDto {
    @NotBlank(message = "가게명은 필수값입니다.")
    private String name;
    @NotBlank(message = "가게설명은 필수값입니다.")
    private String description;
    @NotBlank(message = "주소는 필수값입니다.")
    private String address;
    @ImageUrlPattern
    private String imageUrl;
    @Min(value = 0, message = "수용테이블은 0 미만일 수 없습니다.")
    private Integer capacity;
    @NotNull(message = "시작시간은 필수값입니다.")
    @HalfHourOnly // 00분 또는 30분 단위
    private LocalTime startTime;
    @NotNull(message = "마감시간은 필수값입니다.")
    @HalfHourOnly
    private LocalTime endTime;
    @Min(value = 0, message = "예약금은 0 미만일 수 없습니다.")
    private Integer deposit;
    @NotNull(message = "카테고리는 필수값입니다.")
    private Long categoryId;

    @Builder
    public StoreCreateRequestDto(String name, String description, String address, String imageUrl, Integer capacity, LocalTime startTime, LocalTime endTime, Integer deposit, Long categoryId) {
        this.name = name;
        this.description = description;
        this.address = address;
        this.imageUrl = imageUrl;
        this.capacity = capacity;
        this.startTime = startTime;
        this.endTime = endTime;
        this.deposit = deposit;
        this.categoryId = categoryId;
    }
}
