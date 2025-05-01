package org.example.tablenow.domain.store.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.annotations.WriteTypeHint;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.example.tablenow.global.constant.TimeConstants.TIME_HH_MM;
import static org.example.tablenow.global.constant.TimeConstants.ZONE_ID_ASIA_SEOUL;
import static org.example.tablenow.global.util.TimeFormatUtil.isValidUtcIso8601;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(indexName = "store", writeTypeHint = WriteTypeHint.FALSE)
@Setting(settingPath = "elastic/store-setting.json")
@Mapping(mappingPath = "elastic/store-mapping.json")
@JsonIgnoreProperties(ignoreUnknown = true)
public class StoreDocument {
    @Id
    private Long id;
    private String name;
    private String description;
    private String address;
    private String imageUrl;
    private Integer capacity;
    private Integer deposit;
    private Double rating;
    private Integer ratingCount;
    private String startTime;
    private String endTime;
    private Long userId;
    private String userName; // 가게사장 이름 비정규화
    private Long categoryId;
    private String categoryName; // 카테고리 명 비정규화
    private LocalDateTime deletedAt;

    @Builder
    public StoreDocument(Long id, String name, String description, String address, String imageUrl, Integer capacity, Integer deposit, Double rating, Integer ratingCount, String startTime, String endTime, Long userId, String userName, Long categoryId, String categoryName, LocalDateTime deletedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.address = address;
        this.imageUrl = imageUrl;
        this.capacity = capacity;
        this.deposit = deposit;
        this.rating = rating;
        this.ratingCount = ratingCount;
        this.startTime = startTime;
        this.endTime = endTime;
        this.userId = userId;
        this.userName = userName;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.deletedAt = deletedAt;
    }

    public static StoreDocument fromStore(Store store) {
        return StoreDocument.builder()
                .id(store.getId())
                .name(store.getName())
                .description(store.getDescription())
                .address(store.getAddress())
                .imageUrl(store.getImageUrl())
                .capacity(store.getCapacity())
                .deposit(store.getDeposit())
                .rating(store.getRating())
                .ratingCount(store.getRatingCount())
                .startTime(String.valueOf(store.getStartTime()))
                .endTime(String.valueOf(store.getEndTime()))
                .userId(store.getUserId())
                .userName(store.getUserName())
                .categoryId(store.getCategoryId())
                .categoryName(store.getCategoryName())
                .deletedAt(store.getDeletedAt())
                .build();
    }

    public void convertTimeFormat() {
        this.startTime = convertUtcIsoToKstHHmm(this.startTime);
        this.endTime = convertUtcIsoToKstHHmm(this.endTime);
    }

    private String convertUtcIsoToKstHHmm(String timeStr) {
        if (isValidUtcIso8601(timeStr)) {
            ZonedDateTime utcTime = ZonedDateTime.parse(timeStr);
            ZonedDateTime seoulTime = utcTime.withZoneSameInstant(ZONE_ID_ASIA_SEOUL);
            return seoulTime.format(DateTimeFormatter.ofPattern(TIME_HH_MM));
        }
        return timeStr;
    }
}
