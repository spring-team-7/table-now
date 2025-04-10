package org.example.tablenow.domain.store.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.tablenow.domain.store.validation.annotation.HalfHourOnly;

import java.time.LocalTime;

@Getter
@NoArgsConstructor
public class StoreCreateRequestDto {
    @NotBlank
    private String name;
    @NotBlank
    private String description;
    @NotBlank
    private String address;
    private String imageUrl; // TODO S3 이미지 업로드
    @Min(0)
    private Integer capacity;
    @NotNull
    @HalfHourOnly // 00분 또는 30분 단위
    private LocalTime startTime;
    @NotNull
    @HalfHourOnly
    private LocalTime endTime;
    @Min(0)
    private Integer deposit;
    @NotNull
    private Long categoryId;
}
