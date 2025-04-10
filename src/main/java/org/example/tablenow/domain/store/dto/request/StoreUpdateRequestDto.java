package org.example.tablenow.domain.store.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.store.validation.annotation.HalfHourOnly;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class StoreUpdateRequestDto {
    private String name;
    private String description;
    private String address;
    private String imageUrl; // TODO S3 이미지 업로드
    @Min(0)
    private Integer capacity;
    @HalfHourOnly // 00분 또는 30분 단위
    private LocalTime startTime;
    @HalfHourOnly
    private LocalTime endTime;
    @Min(0)
    private Integer deposit;
    private Long categoryId;
}
