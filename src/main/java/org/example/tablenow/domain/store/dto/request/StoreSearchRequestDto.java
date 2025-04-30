package org.example.tablenow.domain.store.dto.request;

import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreSearchRequestDto {
    @Positive
    private int page = 1;
    @Positive
    private int size = 10;
    private String sort = "ratingCount";
    private String direction = "desc";
    private Long categoryId;
    private String keyword;
}
