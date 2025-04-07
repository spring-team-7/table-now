package org.example.tablenow.domain.store.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
public class StoreRankingResponseDto {
    private final Integer rank;
    private final String keyword;
    private final Integer score;

    @Builder
    public StoreRankingResponseDto(Integer rank, String keyword, Integer score) {
        this.rank = rank;
        this.keyword = keyword;
        this.score = score;
    }
}
