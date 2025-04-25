package org.example.tablenow.domain.notification.message.vacancy.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class VacancyEventDto {

    private final Long storeId;
    private final LocalDate waitDate;

    @Builder
    private VacancyEventDto(Long storeId, LocalDate waitDate) {
        this.storeId = storeId;
        this.waitDate = waitDate;
    }

    public static VacancyEventDto from(Long storeId, LocalDate waitDate) {
        return VacancyEventDto.builder()
            .storeId(storeId)
            .waitDate(waitDate)
            .build();
    }
}