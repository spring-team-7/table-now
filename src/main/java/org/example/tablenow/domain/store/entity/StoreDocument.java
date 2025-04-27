package org.example.tablenow.domain.store.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Mapping;
import org.springframework.data.elasticsearch.annotations.Setting;
import org.springframework.data.elasticsearch.annotations.WriteTypeHint;

@Getter
@NoArgsConstructor
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
    private String deletedAt;

    @Builder
    public StoreDocument(Long id, String name, String description, String address, String imageUrl, Integer capacity, Integer deposit, Double rating, Integer ratingCount, String startTime, String endTime, Long userId, String userName, Long categoryId, String categoryName, String deletedAt) {
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
                .userId(store.getUser().getId())
                .userName(store.getUser().getName())
                .categoryId(store.getCategory().getId())
                .categoryName(store.getCategory().getName())
                .deletedAt(String.valueOf(store.getDeletedAt()))
                .build();
    }
}
